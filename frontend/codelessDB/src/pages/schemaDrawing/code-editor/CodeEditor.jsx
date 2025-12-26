import { useState, useEffect } from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { sql } from '@codemirror/lang-sql';
import { optimizeSQLWithGemini, updateDDL, getDDL } from "./fetch.js";
import { generateSQLFromBackend } from "../fetch.js";
import { validateSchema } from "../generate/CheckCorrectness";
import { convertToJSON } from "../generate/JsonConverter";
import ConfirmationModal from '../../../components/ConfirmationModal/ConfirmationModal.jsx';
import { useAuth } from '../../../components/AuthProvider.jsx';
import { useNotification } from '../../../components/NotificationContext';
import './CodeEditor.css';
import { useNavigate } from "react-router-dom";
import { parse } from "sql-parser-cst";

const MAX_AI_QUOTA = parseInt(import.meta.env.VITE_MAX_AI_QUOTA) || 5;

function CodeEditor({ onClose, diagramId, schemaName, nodes }) {

  const removeUseStatements = (sql) => {
    const statements = sql
      .split(";")
      .map(s => s.trim())
      .filter(Boolean)
      .filter(stmt => !/^USE\s+/i.test(stmt));
    return statements.map(s => s + ";").join("\n\n");
  };

  // Tab states
  const [activeTab, setActiveTab] = useState('editable'); // 'editable', 'generated', 'optimized'
  const [tabs, setTabs] = useState({
    editable: true,
    generated: false,
    optimized: false
  });

  // Code states
  const [editableCode, setEditableCode] = useState('');
  const [generatedCode, setGeneratedCode] = useState('');
  const [optimizedCode, setOptimizedCode] = useState('');
  const [optimizationSummary, setOptimizationSummary] = useState('');

  // UI states
  const [isOptimizing, setIsOptimizing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [canGenerate, setCanGenerate] = useState(true);
  const [nodesSnapshot, setNodesSnapshot] = useState(JSON.stringify(nodes));

  // Error states
  const [errors, setErrors] = useState({
    editable: null,
    generated: null,
    optimized: null,
    optimization: null
  });

  // Modal states
  const [modals, setModals] = useState({
    close: false,
    applyOptimized: false,
    applyGenerated: false
  });

  const { showSuccess, showError } = useNotification();
  const { user, updateUserQuota } = useAuth();
  const aiQuota = user?.aiQuotaRemaining ?? MAX_AI_QUOTA;
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDDL = async () => {
      try {
        const ddl = await getDDL(diagramId);
        setEditableCode(ddl);
      } catch (err) {
        showError(err.message || 'Failed to fetch DDL');
      }
    };
    if (diagramId) fetchDDL();
  }, [diagramId]);

  useEffect(() => {
    const currentSnapshot = JSON.stringify(nodes);
    if (currentSnapshot !== nodesSnapshot) {
      setCanGenerate(true);
    }
  }, [nodes, nodesSnapshot]);

  const validateSQL = (value, tabName) => {
    try {
      parse(value.trim(), { dialect: "mysql" });
      setErrors(prev => ({ ...prev, [tabName]: null }));
      return true;
    } catch (err) {
      setErrors(prev => ({ ...prev, [tabName]: err.message }));
      return false;
    }
  };

  const handleCodeChange = (value) => {
    if (activeTab === 'editable') {
      setEditableCode(value);
      setErrors(prev => ({ ...prev, editable: null }));
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

  const handleGenerateSQL = async () => {
    const validation = validateSchema(nodes);
    if (!validation.isValid) {
      showError(`Validation Failed:\n- ${validation.errors.join("\n- ")}`);
      return;
    }

    setIsGenerating(true);
    setErrors(prev => ({ ...prev, optimization: null }));

    try {
      const finalJson = convertToJSON(schemaName, nodes);
      const data = await generateSQLFromBackend(finalJson);
      const cleanedData = removeUseStatements(data);

      setGeneratedCode(cleanedData);
      validateSQL(cleanedData, 'generated');
      setTabs(prev => ({ ...prev, generated: true }));
      setActiveTab('generated');
      setCanGenerate(false);
      setNodesSnapshot(JSON.stringify(nodes));
    } catch (err) {
      showError(err.message || 'Failed to generate SQL');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleAIOptimize = async () => {
    setIsOptimizing(true);
    setErrors(prev => ({ ...prev, optimization: null }));

    try {
      const result = await optimizeSQLWithGemini(editableCode);
      setOptimizedCode(result.optimizedSQL);
      setOptimizationSummary(result.summary);
      validateSQL(result.optimizedSQL, 'optimized');

      if (user) updateUserQuota(aiQuota - 1);

      setTabs(prev => ({ ...prev, optimized: true }));
      setActiveTab('optimized');
    } catch (err) {
      setErrors(prev => ({
        ...prev,
        optimization: err.message?.includes('quota') ? err.message : 'Failed to optimize SQL.'
      }));
    } finally {
      setIsOptimizing(false);
    }
  };

  const handleApplyOptimized = () => {
    setModals(prev => ({ ...prev, applyOptimized: true }));
  };

  const confirmApplyOptimized = () => {
    setEditableCode(optimizedCode);
    setErrors(prev => ({ ...prev, editable: null }));
    setTabs(prev => ({ ...prev, optimized: false }));
    setOptimizedCode('');
    setOptimizationSummary('');
    setActiveTab('editable');
    setModals(prev => ({ ...prev, applyOptimized: false }));
    showSuccess('Optimized SQL applied to main editor');
  };

  const handleApplyGenerated = () => {
    setModals(prev => ({ ...prev, applyGenerated: true }));
  };

  const confirmApplyGenerated = () => {
    setEditableCode(generatedCode);
    setErrors(prev => ({ ...prev, editable: null }));
    setTabs(prev => ({ ...prev, generated: false }));
    setGeneratedCode('');
    setActiveTab('editable');
    setCanGenerate(true);
    setModals(prev => ({ ...prev, applyGenerated: false }));
    showSuccess('Generated SQL applied to main editor');
  };

  const handleSave = async () => {
    if (!diagramId) return;

    if (!validateSQL(editableCode, 'editable')) {
      showError('Cannot save SQL with syntax errors');
      return;
    }

    setIsSaving(true);
    try {
      const resp = await updateDDL(diagramId, editableCode);
      showSuccess(resp || 'DDL saved successfully');
    } catch (err) {
      showError(err.message || "Failed to save DDL");
    } finally {
      setIsSaving(false);
    }
  };

  const handleCreateDatabase = () => {
    if (!validateSQL(editableCode, 'editable')) {
      showError('Cannot create database with SQL syntax errors');
      return;
    }
    sessionStorage.setItem("sql", editableCode);
    navigate("/database-configuration");
  };

  const handleClose = () => {
    setModals(prev => ({ ...prev, close: true }));
  };

  const confirmClose = () => {
    setOptimizedCode('');
    setGeneratedCode('');
    setOptimizationSummary('');
    onClose();
  };

  const getCurrentCode = () => {
    switch (activeTab) {
      case 'generated': return generatedCode;
      case 'optimized': return optimizedCode;
      default: return editableCode;
    }
  };

  const getCurrentError = () => {
    return errors[activeTab];
  };

  const isCurrentTabEditable = () => {
    return activeTab === 'editable';
  };

  return (
    <div className='code-container'>
      <div className='code-editor-wrapper'>
        <div className='warning-close-wrapper'>
          <div className="sql-warning">
            This SQL Doesn't reflect the diagram, Changes in the diagram will <strong>not</strong> update this code.
          </div>
          <button className="close-btn" onClick={handleClose}>✕</button>
        </div>

        {/* Tab Navigation */}
        <div className="tab-navigation">
          <button
            className={`tab-btn ${activeTab === 'editable' ? 'active' : ''}`}
            onClick={() => setActiveTab('editable')}
          >
            Main Editor
          </button>

          {tabs.generated && (
            <button
              className={`tab-btn ${activeTab === 'generated' ? 'active' : ''}`}
              onClick={() => setActiveTab('generated')}
            >
              Generated SQL
            </button>
          )}

          {tabs.optimized && (
            <button
              className={`tab-btn ${activeTab === 'optimized' ? 'active' : ''}`}
              onClick={() => setActiveTab('optimized')}
            >
              AI Optimized
            </button>
          )}

          <div className="ai-optimize-section">
            {activeTab === 'editable' && (
              <>
                <span style={{
                  display: 'flex',
                  alignItems: 'center',
                  fontSize: '12px',
                  color: aiQuota <= 0 ? '#f44336' : '#ffffff',
                  marginRight: '12px',
                  fontWeight: 500
                }}>
                  {getResetTimeMessage()}
                </span>
                <button
                  className="ai-optimize-btn"
                  onClick={handleAIOptimize}
                  disabled={isOptimizing || !editableCode || aiQuota <= 0}
                  style={{ marginRight: '8px' }}
                >
                  {isOptimizing ? (
                    <><span className="spinner"></span> Optimizing...</>
                  ) : (
                    <> AI Optimize ({aiQuota}/{MAX_AI_QUOTA})</>
                  )}
                </button>
                <button
                  className="editor-btn"
                  onClick={handleGenerateSQL}
                  disabled={!canGenerate || isGenerating}
                  style={{ background: '#10b981', marginRight: '8px' }}
                >
                  {isGenerating ? (
                    <><span className="spinner"></span> Generating...</>
                  ) : (
                    <>Generate</>
                  )}
                </button>
                <button
                  className="editor-btn"
                  onClick={handleSave}
                  disabled={isSaving || !diagramId}
                  style={{ backgroundColor: '#4b5563', marginRight: '8px' }}
                >
                  {isSaving ? 'Saving...' : 'Save'}
                </button>
                <button
                  className="editor-btn"
                  onClick={handleCreateDatabase}
                  style={{ marginRight: 0 }}
                >
                  Create Database
                </button>
              </>
            )}

            {activeTab === 'generated' && (
              <button
                className="editor-btn"
                onClick={handleApplyGenerated}
                style={{ marginRight: 0 }}
              >
                Apply to Main Editor
              </button>
            )}

            {activeTab === 'optimized' && (
              <button
                className="editor-btn"
                onClick={handleApplyOptimized}
                style={{ marginRight: 0 }}
              >
                Apply to Main Editor
              </button>
            )}
          </div>
        </div>

        {/* Error Messages */}
        {errors.optimization && (
          <div className="optimization-error">{errors.optimization}</div>
        )}

        {activeTab === 'optimized' && optimizationSummary && (
          <div className="optimization-summary">
            <strong>Optimization Summary:</strong>
            <p>{optimizationSummary}</p>
          </div>
        )}

        {getCurrentError() && (
          <div className="sql-error">
            {getCurrentError()}
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
            editable={isCurrentTabEditable()}
            readOnly={!isCurrentTabEditable()}
          />
        </div>
      </div>

      {/* Confirmation Modals */}
      <ConfirmationModal
        isOpen={modals.close}
        onConfirm={confirmClose}
        onCancel={() => setModals(prev => ({ ...prev, close: false }))}
        title="Close Code Editor?"
        message="Any unsaved changes in the main editor will be lost. Generated and optimized tabs will be cleared."
        confirmText="Close"
        cancelText="Stay"
        confirmButtonStyle="primary"
      />

      <ConfirmationModal
        isOpen={modals.applyOptimized}
        onConfirm={confirmApplyOptimized}
        onCancel={() => setModals(prev => ({ ...prev, applyOptimized: false }))}
        title="Apply Optimized SQL?"
        message="This will replace the content in your main editor with the optimized SQL. The optimization tab will be closed."
        confirmText="Apply"
        cancelText="Cancel"
        confirmButtonStyle="primary"
      />

      <ConfirmationModal
        isOpen={modals.applyGenerated}
        onConfirm={confirmApplyGenerated}
        onCancel={() => setModals(prev => ({ ...prev, applyGenerated: false }))}
        title="Apply Generated SQL?"
        message="This will replace the content in your main editor with the generated SQL. You'll be able to generate again if the schema changes."
        confirmText="Apply"
        cancelText="Cancel"
        confirmButtonStyle="primary"
      />
    </div>
  );
}

export default CodeEditor;
