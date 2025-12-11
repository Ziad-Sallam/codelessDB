import { useState } from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { sql } from '@codemirror/lang-sql';
import { optimizeSQLWithGemini } from "./optimize.js";
import ConfirmationModal from '../../../components/ConfirmationModal/ConfirmationModal.jsx';
import { useAuth } from '../../../components/AuthProvider.jsx';
import './CodeEditor.css';

const MAX_AI_QUOTA = parseInt(import.meta.env.VITE_MAX_AI_QUOTA) || 5;

function CodeEditor({ initialCode, onClose }) {
  const [code, setCode] = useState(initialCode || '');
  const [activeTab, setActiveTab] = useState('generated'); // 'generated' or 'optimized'
  const [optimizedSQL, setOptimizedSQL] = useState('');
  const [optimizationSummary, setOptimizationSummary] = useState('');
  const [isOptimizing, setIsOptimizing] = useState(false);
  const [optimizationError, setOptimizationError] = useState('');
  const [showModal, setShowModal] = useState(false);

  const { user, updateUserQuota } = useAuth();
  const aiQuota = user?.aiQuotaRemaining ?? MAX_AI_QUOTA;

  // Calculate time until quota reset (midnight)
  const getResetTimeMessage = () => {
    const now = new Date();
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);

    const hoursUntilReset = Math.floor((tomorrow - now) / (1000 * 60 * 60));
    const minutesUntilReset = Math.floor(((tomorrow - now) % (1000 * 60 * 60)) / (1000 * 60));

    if (hoursUntilReset > 0) {
      return `Resets in ${hoursUntilReset}h ${minutesUntilReset}m`;
    } else {
      return `Resets in ${minutesUntilReset}m`;
    }
  };

  const handleAIOptimize = async () => {
    setIsOptimizing(true);
    setOptimizationError('');

    try {
      const result = await optimizeSQLWithGemini(code);
      setOptimizedSQL(result.optimizedSQL);
      setOptimizationSummary(result.summary);

      // Update quota in auth context
      if (user) {
        updateUserQuota(aiQuota - 1);
      }

      setActiveTab('optimized');
      console.log('AI Optimize clicked - implement Gemini integration');
    } catch (error) {
      // Check if it's a quota error
      if (error.message && error.message.includes('quota')) {
        setOptimizationError(error.message);
      } else {
        setOptimizationError('Failed to optimize SQL. Please try again.');
      }
      console.error('Optimization error:', error);
    } finally {
      setIsOptimizing(false);
    }
  };

  const getCurrentCode = () => {
    return activeTab === 'generated' ? code : optimizedSQL;
  };

  const handleCodeChange = (value) => {
    if (activeTab === 'generated') {
      setCode(value);
    } else {
      setOptimizedSQL(value);
    }
  };

  const handleClose = () => {
    setShowModal(true)
  }

  const handleModalConfirm = () => {
    setOptimizedSQL('');
    setOptimizationSummary('');
    onClose()
    setShowModal(false);
  };

  const handleModalCancel = () => {
    setShowModal(false);
  };

  return (
    <div className='code-container'>
      <div className='code-editor-wrapper'>
        <div className='warning-close-wrapper'>
          <div className="sql-warning">
            This SQL reflects the diagram at the time it was generated.
            <br />
            Changes in the diagram will
            <strong> not </strong>
            update this code.
          </div>

          <button className="close-btn" onClick={handleClose}>✕</button>
        </div>

        {/* Tab Navigation */}
        <div className="tab-navigation">
          <button
            className={`tab-btn ${activeTab === 'generated' ? 'active' : ''}`}
            onClick={() => setActiveTab('generated')}
          >
            Generated SQL
          </button>
          {optimizedSQL && (
            <button
              className={`tab-btn ${activeTab === 'optimized' ? 'active' : ''}`}
              onClick={() => setActiveTab('optimized')}
            >
              Optimized SQL
            </button>
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
                {isOptimizing ? (
                  <>
                    <span className="spinner"></span>
                    Optimizing...
                  </>
                ) : (
                  <>
                    <span className="ai-icon">✨</span>
                    AI Optimize ({aiQuota}/{MAX_AI_QUOTA})
                  </>
                )}
              </button>
            </div>
          )}
        </div>

        {/* Error Message */}
        {optimizationError && (
          <div className="optimization-error">
            {optimizationError}
          </div>
        )}

        {/* Optimization Summary */}
        {activeTab === 'optimized' && optimizationSummary && (
          <div className="optimization-summary">
            <strong>Optimization Summary:</strong>
            <p>{optimizationSummary}</p>
          </div>
        )}

        {/* Code Editor */}
        <div style={{ overflow: "auto" }}>
          <CodeMirror
            value={getCurrentCode()}
            extensions={[sql()]}
            onChange={handleCodeChange}
            theme='dark'
            height='100%'
          />
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