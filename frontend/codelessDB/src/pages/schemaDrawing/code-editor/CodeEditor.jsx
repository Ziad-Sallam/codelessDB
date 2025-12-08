import { useState } from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { sql } from '@codemirror/lang-sql';
import './CodeEditor.css';
import { useNavigate } from "react-router-dom";

function CodeEditor({ initialCode, onClose }) {
  const [code, setCode] = useState(initialCode || '');
  const navigate = useNavigate();

  const handleAction = () => {
    sessionStorage.setItem("sql", code);
    navigate("/database-configuration");
  };

  return (
    <div className='code-container'>
      <div className='code-editor-wrapper'>
        <div className='warning-close-wrapper'>
          <div className="sql-warning">
            This SQL reflects the diagram at the time it was generated.
            <br />
            Changes in the diagram will <strong>not</strong> update this code.
          </div>

          <button className="close-btn" onClick={onClose}>✕</button>
        </div>
        <CodeMirror
          value={code}
          extensions={[sql()]}
          onChange={(value) => setCode(value)}
          theme='dark'
        />

        <div className="editor-footer">
          <button className="editor-btn" onClick={handleAction}>
            Create Database
          </button>
        </div>
      </div>
    </div>
  );
}

export default CodeEditor;