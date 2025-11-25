import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import CodeMirror from '@uiw/react-codemirror';
import { sql } from '@codemirror/lang-sql';
import './CodeEditor.css';

function CodeEditor() {
  const navigate = useNavigate();

  // Replace this with your actual SQL code from the diagram generator
  const [code, setCode] = useState(
  `-- Generated SQL Code
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  total_amount DECIMAL(10, 2) NOT NULL,
  order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);`
);

  const [hasChanges, setHasChanges] = useState(false);

  const handleCodeChange = (value) => {
    setCode(value);
    setHasChanges(true);
  };

  const handleBackToDiagram = () => {
    if (hasChanges) {
      const confirmLeave = window.confirm(
        'Warning: You have unsaved changes. Going back to the diagram will discard all your edits. Do you want to continue?'
      );
      if (!confirmLeave) return;
    }
    navigate('/');
  };

  // const handleExecuteSQL = () => {
  //   console.log('Executing SQL:', sqlCode);

  //   alert('SQL execution feature - implement your logic here');
  // };

  return (
    <div className='code-container'>
      <button
        className='back-button'
        onClick={handleBackToDiagram}
      >
        Back to Diagram
      </button>

      <div className='code-editor-wrapper'>
        <CodeMirror
          value={code}
          extensions={[sql()]}
          onChange={handleCodeChange}
          theme='light'
          height='100%'
        />
      </div>

      {/* <button
        className='execute-button'
        onClick={handleExecuteSQL}
      >
        Download SQL
      </button> */}

    </div>
  );
}

export default Code;