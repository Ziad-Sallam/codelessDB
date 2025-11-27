import { useState } from 'react';
import CodeMirror from '@uiw/react-codemirror';
import { sql } from '@codemirror/lang-sql';
import './CodeEditor.css';

function CodeEditor({ initialCode, onClose }) {

  const [code, setCode] = useState(initialCode || '');

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

          <button className="close-btn" onClick={onClose}>✕</button>
        </div>
        
        <CodeMirror
          value={code}
          extensions={[sql()]}
          onChange={(value) => setCode(value)}
          theme='dark'
          height='100%'
        />
      </div>

    </div>
  );
}

export default CodeEditor;