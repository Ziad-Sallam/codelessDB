import { useState } from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { sql } from '@codemirror/lang-sql';
import { optimizeSQLWithGemini } from "./optimize.js";
import { updateDDL } from "./optimize.js";
import ConfirmationModal from '../../../components/ConfirmationModal/ConfirmationModal.jsx';
import { useAuth } from '../../../components/AuthProvider.jsx';
import { useNotification } from '../../../components/NotificationContext';
import './CodeEditor.css';
import { useNavigate } from "react-router-dom";
import { parse } from "sql-parser-cst";

const MAX_AI_QUOTA = parseInt(import.meta.env.VITE_MAX_AI_QUOTA) || 5;

function CodeEditor({ initialCode, onClose, diagramId }) {
  const removeUseStatements = (sql) => {
    const statements = sql
      .split(";")
      .map(s => s.trim())
      .filter(Boolean)
      .filter(stmt => !/^USE\s+/i.test(stmt));
    return statements.map(s => s + ";").join("\n\n");
  };

  const [code, setCode] = useState(removeUseStatements(initialCode) || '');
  const [optimizedSQL, setOptimizedSQL] = useState('');
  const [activeTab, setActiveTab] = useState('generated'); // 'generated' or 'optimized'
  const [optimizationSummary, setOptimizationSummary] = useState('');
  const [isOptimizing, setIsOptimizing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [optimizationError, setOptimizationError] = useState('');
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);

  const { showSuccess, showError } = useNotification();
  const { user, updateUserQuota } = useAuth();
  const aiQuota = user?.aiQuotaRemaining ?? MAX_AI_QUOTA;
  const navigate = useNavigate();

  const handleAction = () => {
    sessionStorage.setItem("sql", code);
    navigate("/database-configuration");
  };

  const handleCodeChange = (value) => {
    try {
      parse(value.trim(), { dialect: "mysql" });
      setError(null);
    } catch (err) {
      setError(err.message);
    }

    if (activeTab === 'generated') {
      setCode(value);
    } else {
      setOptimizedSQL(value);
    }
  };

  const getResetTimeMessage = () => {
    const now = new Date();
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);

    const hoursUntilReset = Math.floor((tomorrow - now) / (1000 * 60 * 60));
    const minutesUntilReset = Math.floor(((tomorrow - now) % (1000 * 60 * 60)) / (1000 * 60));

    return hoursUntilReset > 0
      ? `Resets in ${hoursUntilReset}h ${minutesUntilReset}m`
      : `Resets in ${minutesUntilReset}m`;
  };

  const handleAIOptimize = async () => {
    setIsOptimizing(true);
    setOptimizationError('');

    try {
      const result = await optimizeSQLWithGemini(code);
      setOptimizedSQL(result.optimizedSQL);
      setOptimizationSummary(result.summary);

      if (user) updateUserQuota(aiQuota - 1);

      setActiveTab('optimized');
    } catch (err) {
      setOptimizationError(err.message?.includes('quota') ? err.message : 'Failed to optimize SQL.');
    } finally {
      setIsOptimizing(false);
    }
  };

  const handleUpdateDDL = async () => {
    if (!diagramId) return;
    setIsSaving(true);
    try {
      const currentCode = getCurrentCode();
      const resp = await updateDDL(diagramId, currentCode);
      showSuccess(resp);
    } catch (err) {
      showError(err.message || "Failed to save DDL");
    } finally {
      setIsSaving(false);
    }
  };

  const handleClose = () => setShowModal(true);
  const handleModalConfirm = () => {
    setOptimizedSQL('');
    setOptimizationSummary('');
    onClose();
    setShowModal(false);
  };
  const handleModalCancel = () => setShowModal(false);

  const getCurrentCode = () => (activeTab === 'generated' ? code : optimizedSQL);

  return (
    <div className='code-container'>
      <div className='code-editor-wrapper'>
        <div className='warning-close-wrapper'>
          <div className="sql-warning">
            This SQL reflects the diagram at the time it was generated.
            <br />
            Changes in the diagram will <strong>not</strong> update this code.
          </div>
          <button className="close-btn" onClick={handleClose}>✕</button>
        </div>

        {/* Tab Navigation */}
        <div className="tab-navigation">
          <button className={`tab-btn ${activeTab === 'generated' ? 'active' : ''}`} onClick={() => setActiveTab('generated')}>Generated SQL</button>
          {optimizedSQL && (
            <button className={`tab-btn ${activeTab === 'optimized' ? 'active' : ''}`} onClick={() => setActiveTab('optimized')}>Optimized SQL</button>
          )}

          {activeTab === 'generated' && (
            <div className="ai-optimize-section">
              <span style={{
                display: 'flex',
                alignItems: 'center',
                fontSize: '12px',
                color: aiQuota <= 0 ? '#f44336' : '#666',
                marginRight: '12px',
                fontWeight: 500
              }}>
                {getResetTimeMessage()}
              </span>
              <button
                className="ai-optimize-btn"
                onClick={handleAIOptimize}
                disabled={isOptimizing || !code || aiQuota <= 0}
              >
                {isOptimizing ? <> <span className="spinner"></span> Optimizing... </> : <> <span className="ai-icon">✨</span> AI Optimize ({aiQuota}/{MAX_AI_QUOTA}) </>}
              </button>
            </div>
          )}
        </div>

        {/* Error Messages */}
        {optimizationError && <div className="optimization-error">{optimizationError}</div>}
        {activeTab === 'optimized' && optimizationSummary && (
          <div className="optimization-summary">
            <strong>Optimization Summary:</strong>
            <p>{optimizationSummary}</p>
          </div>
        )}
        {error && (
          <div className="sql-error">
            <strong>Syntax Error:</strong> {error}
          </div>
        )}

        {/* Code Editor */}
        <div style={{ overflow: "auto" }}>
          <CodeMirror
            value={getCurrentCode()}
            extensions={[sql()]}
            onChange={handleCodeChange}
            theme='dark'
            height='400px'
          />
        </div>

        {/* Footer */}
        <div className="editor-footer">
          <button
            className="editor-btn"
            onClick={handleAction}
            disabled={!!error}
          >
            Create Database
          </button>

          {diagramId && (
            <button
              className="editor-btn save-ddl-btn"
              onClick={handleUpdateDDL}
              disabled={isSaving || !!error}
              style={{ marginLeft: '10px', backgroundColor: '#4b5563' }}
            >
              {isSaving ? "Saving..." : "Save"}
            </button>
          )}
        </div>
      </div>

      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={showModal}
        onConfirm={handleModalConfirm}
        onCancel={handleModalCancel}
        title="Discard Optimized SQL?"
        message="Switching back to the Diagram will erase your optimized SQL. Continue?"
        confirmText="Go to Diagram"
        cancelText="Stay Here"
        confirmButtonStyle="primary"
      />
    </div>
  );
}

export default CodeEditor;
