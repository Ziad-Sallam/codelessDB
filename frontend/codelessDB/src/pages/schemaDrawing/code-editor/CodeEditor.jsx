import { useState } from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { sql } from '@codemirror/lang-sql';
import './CodeEditor.css';
import { useNavigate } from "react-router-dom";
import { parse } from "sql-parser-cst";

function CodeEditor({ initialCode, onClose }) {
  const removeUseStatements = (sql) => {

  const statements = sql
    .split(";")
    .map(s => s.trim())
    .filter(Boolean)
    .filter(stmt => !/^USE\s+/i.test(stmt));


  return statements.map(s => s + ";").join("\n\n");
};

  const [code, setCode] = useState(removeUseStatements(initialCode) || '');
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleAction = () => {
    sessionStorage.setItem("sql", code);
    navigate("/database-configuration");
  };



  const handleChange = (value) => {
    setCode(value);
    try {
      console.log(value)
      parse(value.trim(), {
        dialect: "mysql" 
      });       
      setError(null);
    } catch (err) {
      setError(err.message);
    }
    console.log(error)
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
          onChange={handleChange}
          theme='dark'
        />


        <div className="editor-footer">
          <div className="error-wrapper">
            {error && (
              <div className="sql-error">
                <strong>Syntax Error:</strong> {error}
              </div>
            )}
          </div>

          <button 
            className="editor-btn" 
            onClick={handleAction}
            disabled={!!error}
          >
            Create Database
          </button>
        </div>
      </div>
    </div>
  );
}

export default CodeEditor;