import React, { useState } from 'react';
import './databaseManager.css';

// Type Definitions
interface QueryRequest {
  databaseId: number;
  content: string;
}

interface QueryResponse {
  correlationId: string;
  type: string | null;
  columns: string[] | null;
  rows: any[][] | null;
  rowCount: number;
  success: boolean;
  message: string | null;
}

// Mock API function (replace with actual API call)
const queryApi = {
  executeQuery: async (request: QueryRequest): Promise<QueryResponse> => {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Mock response based on query content
    if (request.content.toLowerCase().includes('error') || request.content.toLowerCase().includes('tabddd')) {
      return {
        correlationId: crypto.randomUUID(),
        type: null,
        columns: null,
        rows: null,
        rowCount: 0,
        success: false,
        message: "1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 'TABdddLES' at line 1"
      };
    }
    
    // Mock successful response
    return {
      correlationId: crypto.randomUUID(),
      type: 'SELECT',
      columns: ['Tables_in_Database', "123"],
      rows: [
        ['Entity_1', "1"],
        ['Entity_2', "2"],
        ['Entity_3', "3"]
      ],
      rowCount: 3,
      success: true,
      message: null
    };
  }
};

const QueryRunner: React.FC = () => {
  const [databaseId, setDatabaseId] = useState<string>('');
  const [queryContent, setQueryContent] = useState<string>('');
  const [isExecuting, setIsExecuting] = useState<boolean>(false);
  const [queryResult, setQueryResult] = useState<QueryResponse | null>(null);
  const [hasExecuted, setHasExecuted] = useState<boolean>(false);

  const handleExecuteQuery = async () => {
    // Validation
    if (!databaseId) {
      alert('Please enter a Database ID');
      return;
    }
    if (!queryContent.trim()) {
      alert('Please enter a SQL query');
      return;
    }

    const dbId = parseInt(databaseId);
    if (isNaN(dbId)) {
      alert('Database ID must be a valid number');
      return;
    }

    setIsExecuting(true);
    setHasExecuted(false);
    
    try {
      const request: QueryRequest = {
        databaseId: dbId,
        content: queryContent.trim()
      };
      
      const result = await queryApi.executeQuery(request);
      setQueryResult(result);
      setHasExecuted(true);
    } catch (error) {
      console.error('Failed to execute query:', error);
      setQueryResult({
        correlationId: crypto.randomUUID(),
        type: null,
        columns: null,
        rows: null,
        rowCount: 0,
        success: false,
        message: 'Failed to execute query. Please try again.'
      });
      setHasExecuted(true);
    } finally {
      setIsExecuting(false);
    }
  };

  const handleClearResults = () => {
    setQueryResult(null);
    setHasExecuted(false);
  };

  return (
    <div className="query-runner-container">
      <div className="query-runner-wrapper">
        {/* Header */}
        <div className="query-runner-header">
          <div className="header-title-section">
            <svg className="header-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M4 7h16M4 12h16M4 17h16"></path>
              <circle cx="4" cy="7" r="1" fill="currentColor"></circle>
              <circle cx="4" cy="12" r="1" fill="currentColor"></circle>
              <circle cx="4" cy="17" r="1" fill="currentColor"></circle>
            </svg>
            <h1 className="header-title">SQL Query Runner</h1>
          </div>
          <p className="header-description">
            Execute SQL queries and view results in real-time
          </p>
        </div>

        {/* Query Input Card */}
        <div className="query-card">
          <div className="card-header">
            <div className="card-header-content">
              <div className="card-title-section">
                <h2 className="card-title">
                  <svg className="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                    <polyline points="14 2 14 8 20 8"></polyline>
                    <line x1="16" y1="13" x2="8" y2="13"></line>
                    <line x1="16" y1="17" x2="8" y2="17"></line>
                    <polyline points="10 9 9 9 8 9"></polyline>
                  </svg>
                  Query Input
                </h2>
                <p className="card-description">Enter your database ID and SQL query</p>
              </div>
            </div>
          </div>

          <div className="card-content">
            <div className="form-field">
              <label htmlFor="database-id" className="form-label">
                Database ID <span className="required">*</span>
              </label>
              <input
                id="database-id"
                type="number"
                className="form-input"
                placeholder="e.g., 19"
                value={databaseId}
                onChange={(e) => setDatabaseId(e.target.value)}
                disabled={isExecuting}
              />
            </div>

            <div className="form-field">
              <label htmlFor="query-content" className="form-label">
                SQL Query <span className="required">*</span>
              </label>
              <textarea
                id="query-content"
                className="query-textarea"
                placeholder="e.g., SHOW TABLES"
                rows={8}
                value={queryContent}
                onChange={(e) => setQueryContent(e.target.value)}
                disabled={isExecuting}
              />
            </div>

            <div className="button-group">
              <button
                className="execute-btn primary-btn"
                onClick={handleExecuteQuery}
                disabled={isExecuting}
              >
                <svg className="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polygon points="5 3 19 12 5 21 5 3"></polygon>
                </svg>
                {isExecuting ? 'Executing...' : 'Execute Query'}
              </button>
              {hasExecuted && (
                <button
                  className="clear-btn secondary-btn"
                  onClick={handleClearResults}
                  disabled={isExecuting}
                >
                  <svg className="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                  Clear Results
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Results Card */}
        {hasExecuted && queryResult && (
          <div className="results-card">
            <div className="card-header">
              <div className="card-header-content">
                <div className="card-title-section">
                  <h2 className="card-title">
                    <svg className="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M3 3h18v18H3z"></path>
                      <path d="M3 9h18M3 15h18M9 3v18"></path>
                    </svg>
                    Query Results
                  </h2>
                  <p className="card-description">
                    Correlation ID: {queryResult.correlationId}
                  </p>
                </div>
                <div className="badge-container">
                  <span className={`badge ${queryResult.success ? 'badge-success' : 'badge-error'}`}>
                    {queryResult.success ? 'Success' : 'Failed'}
                  </span>
                  {queryResult.type && (
                    <span className="badge badge-secondary">
                      {queryResult.type}
                    </span>
                  )}
                </div>
              </div>
            </div>

            <div className="card-content">
              {queryResult.success ? (
                <>
                  <div className="result-meta">
                    <div className="meta-item">
                      <span className="meta-label">Row Count:</span>
                      <span className="meta-value">{queryResult.rowCount}</span>
                    </div>
                    {queryResult.columns && (
                      <div className="meta-item">
                        <span className="meta-label">Columns:</span>
                        <span className="meta-value">{queryResult.columns.length}</span>
                      </div>
                    )}
                  </div>

                  {queryResult.columns && queryResult.rows && queryResult.rows.length > 0 ? (
                    <div className="table-container">
                      <table className="results-table">
                        <thead>
                          <tr>
                            {queryResult.columns.map((column, index) => (
                              <th key={index}>{column}</th>
                            ))}
                          </tr>
                        </thead>
                        <tbody>
                          {queryResult.rows.map((row, rowIndex) => (
                            <tr key={rowIndex}>
                              {row.map((cell, cellIndex) => (
                                <td key={cellIndex}>{cell !== null ? cell : <span className="null-value">NULL</span>}</td>
                              ))}
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <div className="empty-result">
                      <svg className="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="12" y1="8" x2="12" y2="12"></line>
                        <line x1="12" y1="16" x2="12.01" y2="16"></line>
                      </svg>
                      <p>Query executed successfully but returned no data</p>
                    </div>
                  )}
                </>
              ) : (
                <div className="error-result">
                  <svg className="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="15" y1="9" x2="9" y2="15"></line>
                    <line x1="9" y1="9" x2="15" y2="15"></line>
                  </svg>
                  <h3 className="error-title">Query Execution Failed</h3>
                  <pre className="error-message">{queryResult.message}</pre>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default QueryRunner;