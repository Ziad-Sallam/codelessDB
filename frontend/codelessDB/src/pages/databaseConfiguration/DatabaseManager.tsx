import React, { useState, useEffect } from 'react';
import './DatabaseManager.css';
import LeftPanel from '../../components/LeftPanel';
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { oneDark } from "react-syntax-highlighter/dist/esm/styles/prism";
import axios from "axios";


interface ServerType {
  serverId: number;
  serverName: string;
}

interface DatabaseConfig {

  databaseName: string;
  databasePassword: string;
  serverId: number | null;
  ddl: string;
}

interface ServerFormData {
  serverName: string;
}
const API_BASE_URL = "http://localhost:8080";

const serverApi = {
  async getUserServers(): Promise<ServerType[]> {
    try {
    const response = await axios.get(
      `${API_BASE_URL}/database/get-user-servers`,
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken")}`,
        },
      }
    );
    console.log(response.data);
    return response.data;
    } catch (error) {
      console.error('Error fetching user servers:', error);
      return [
        { serverId: 101, serverName: 'Production Server' },
        { serverId: 102, serverName: 'Staging Server' },
        { serverId: 103, serverName: 'Development Server' },
      ];
    }
  },

  async createServer(serverData: ServerFormData): Promise<ServerType> {
    try {
      const response = await axios.post(
        `${API_BASE_URL}/database/create-server`,
        serverData,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("authToken")}`,
          },
        }
      );

      return response.data; // should be { serverId, serverName }
    } catch (error) {
      console.error("Error creating server:", error);

      // ✅ fallback mock (kept consistent)
      return {
        serverId: Math.floor(Math.random() * 1000) + 100,
        serverName: serverData.serverName,
      };
    }
  },

  async submitDatabaseConfiguration(
  submissionData: DatabaseConfig
): Promise<any> {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/database/create-database`,
      submissionData,
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken")}`,
        },
      }
    );

    return response.data;
  } catch (error: any) {
    console.error("Error submitting database configuration:", error);

    const message =
      error.response?.data?.message ||
      error.response?.data ||
      "Failed to submit database configuration";

    throw new Error(message);
  }
}


};

const DatabaseManager: React.FC = () => {
  const [availableServers, setAvailableServers] = useState<ServerType[]>([]);
  const [isLoadingServers, setIsLoadingServers] = useState(true);
  const [leftNav, setLeftNav] = useState("all");
  const [database, setDatabase] = useState<DatabaseConfig>({
  
    databaseName: '',
    databasePassword: '',
    serverId: null,
    ddl: ''
  });

  const [isDdlExpanded, setIsDdlExpanded] = useState(false);
  const [selectedServerOption, setSelectedServerOption] = useState<string>('none');
  const [serverForm, setServerForm] = useState<ServerFormData>({
    serverName: '',
  });
  const [isCreatingServer, setIsCreatingServer] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  useEffect(() => {
    const fetchServers = async () => {
      setIsLoadingServers(true);
      try {
        const servers = await serverApi.getUserServers();
        setAvailableServers(servers);
      } catch (error) {
        console.error('Failed to load servers:', error);
      } finally {
        setIsLoadingServers(false);
      }
      const storedText = sessionStorage.getItem("sql");
  
      console.log(storedText);
      if (storedText) {
        setDatabase(prev => ({
          ...prev,
          ddl: storedText.trimEnd() + '\n',
          databaseName: getDatabaseName(storedText)
        }));
      }

    };

    fetchServers();
  }, []);

  function updateDatabaseNameInDDL(ddl: string, newName: string): string {
    if (!newName) return ddl;

    const HEADER_REGEX =
      /^\s*CREATE\s+DATABASE[\s\S]*?;\s*USE\s+[\s\S]*?;\s*/i;

    const rest = ddl.replace(HEADER_REGEX, '');

    return (
      `CREATE DATABASE IF NOT EXISTS ${newName};\n` +
      rest.trimStart()
    );
  }

  function getDatabaseName(sqlString: String){
    const regex =
      /CREATE\s+DATABASE\s+(?:IF\s+NOT\s+EXISTS\s+)?[`"]?([a-zA-Z0-9_]+)[`"]?/i;

    const match = sqlString.match(regex);
    return match ? match[1] : "";
  }

  const handleServerSelection = (value: string) => {
    setSelectedServerOption(value);
    
    if (value === 'new') {
      setDatabase(prev => ({ ...prev, serverId: null }));
    } else if (value === 'none') {
      setDatabase(prev => ({ ...prev, serverId: null }));
    } else {
      const serverId = parseInt(value);
      setDatabase(prev => ({ ...prev, serverId }));
    }
  };

  const handleDatabaseFieldChange = (field: keyof DatabaseConfig, value: string) => {
    setDatabase(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleServerFormChange = (field: keyof ServerFormData, value: string) => {
    setServerForm(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSaveServer = async () => {
    if (!serverForm.serverName) {
      alert('Please fill in the server name');
      return;
    }

    setIsCreatingServer(true);
    try {
      const newServer = await serverApi.createServer(serverForm);
      
      setAvailableServers(prev => [...prev, newServer]);
      setDatabase(prev => ({ ...prev, serverId: newServer.serverId }));
      setSelectedServerOption(newServer.serverId.toString());
      setServerForm({ serverName: '' });
      
      alert(`Server "${newServer.serverName}" created successfully with ID: ${newServer.serverId}`);
    } catch (error) {
      console.error('Failed to create server:', error);
      alert('Failed to create server. Please try again.');
    } finally {
      setIsCreatingServer(false);
    }
  };

const handleSubmitForm = async () => {
  if (!database.databaseName) {
    alert('Please enter a database name');
    return;
  }
  if (database.databaseName.includes(" ")){
    alert("database Name can not contain white spaces")
  }
  if (!database.databasePassword) {
    alert('Please enter a database password');
    return;
  }
  if (selectedServerOption === 'new') {
    alert('Please save the new server first before submitting the form');
    return;
  }
  if (selectedServerOption === 'none') {
    alert('Please Select a Server to deploy your Database');
    return;
  }
  

  const submissionData = {
    databaseName: database.databaseName,
    databasePassword: database.databasePassword,
    serverId: database.serverId!,
    ddl: database.ddl.replace(/[\n\r\t]/g, ""),
  };

  try {
    const result = await serverApi.submitDatabaseConfiguration(submissionData);

    console.log("Submission success:", result);
    alert("Database deployed successfully ✅");
    setDatabase(prev => ({
        ...prev,
        databaseName :'',
        databasePassword: '' 
      }))
  } catch (error: any) {
    alert(error.message);
  }
  
};


  return (
    <div className="databaseManager">
      <LeftPanel leftNav={leftNav} setLeftNav={setLeftNav} />
    <div className="db-manager-container">
      
      <div className="db-manager-wrapper">
        
        <div className="db-manager-header">
          
          <div className="header-title-section">
            
            <svg className="header-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
              <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
              <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
            </svg>
            <h1 className="header-title">Database Configuration Manager</h1>
          </div>
          <p className="header-description">
            Configure your database settings and server information
          </p>
        </div>

        <div className="db-card">
          <div className="card-header">
            <div className="card-header-content">
              <div className="card-title-section">
                <h2 className="card-title">
                  <svg className="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
                    <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
                    <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
                  </svg>
                  {database.databaseName}
                </h2>
                <p className="card-description">
                  Database configuration and server details
                </p>
              </div>
              <div className="badge-container">
                {database.serverId !== null ? (
                  <span className="badge badge-primary">
                    Server ID: {database.serverId}
                  </span>
                ) : (
                  <span className="badge badge-secondary">
                    No Server Assigned
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className="card-content">
            <div className="form-grid">
              <div className="form-field">
                <label htmlFor="database-name" className="form-label">
                  Database Name
                </label>
                <input
                  id="database-name"
                  type="text"
                  className="form-input"
                  placeholder="Enter database name"
                  value={database.databaseName}
                  onChange={(e) => {
                    const value = e.target.value.replace(/\s+/g, '_');

                    setDatabase(prev => ({
                      ...prev,
                      databaseName: value,
                      ddl: updateDatabaseNameInDDL(prev.ddl, value),
                    }));
                  }}


                />
              </div>

              <div className="form-field">
                <label htmlFor="database-password" className="form-label">
                  Database Password
                </label>
                <div className="password-input-wrapper">
                  <input
                    id="database-password"
                    type={showPassword ? "text" : "password"}
                    className="form-input password-input"
                    placeholder="Enter database password"
                    value={database.databasePassword}
                    onChange={(e) => handleDatabaseFieldChange('databasePassword', e.target.value)}
                  />
                  <button
                    type="button"
                    className="password-toggle-btn"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? (
                      <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                        <line x1="1" y1="1" x2="23" y2="23"></line>
                      </svg>
                    ) : (
                      <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                        <circle cx="12" cy="12" r="3"></circle>
                      </svg>
                    )}
                  </button>
                </div>
              </div>
            </div>

            <div className="form-field">
              <label htmlFor="server-select" className="form-label">
                Server Assignment
              </label>
              <select
                id="server-select"
                className="form-select"
                value={selectedServerOption}
                onChange={(e) => handleServerSelection(e.target.value)}
                disabled={isLoadingServers}
              >
                <option value="none">No Server</option>
                {availableServers.map((server) => (
                  <option key={server.serverId} value={server.serverId.toString()}>
                    {server.serverName}
                  </option>
                ))}
                <option value="new">+ Create New Server</option>
              </select>
            </div>

            <div className="form-field">
              <label className="form-label">DDL Statement</label>
              <button
                className="collapsible-trigger highlighted"
                onClick={() => setIsDdlExpanded(!isDdlExpanded)}
              >
                <span>{isDdlExpanded ? 'Hide DDL' : 'Show DDL'}</span>
                <svg 
                  className={`chevron-icon ${isDdlExpanded ? 'rotated' : ''}`}
                  viewBox="0 0 24 24" 
                  fill="none" 
                  stroke="currentColor" 
                  strokeWidth="2"
                >
                  <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
              </button>
              {isDdlExpanded && (
                <SyntaxHighlighter
                  language="sql"
                  style={oneDark}
                  // style={vs}
                  showLineNumbers
                  wrapLongLines
                  customStyle={{
                    borderRadius: "8px",
                    fontSize: "14px",
                    padding: "16px",
                    maxHeight: "400px",
                    overflowY: "auto",
                  }}
                >
                  {database.ddl}
                </SyntaxHighlighter>
              )}
            </div>

            {selectedServerOption === 'new' && (
              <div className="server-form-section">
                <div className="server-form-header">
                  <svg className="server-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="2" y="2" width="20" height="8" rx="2" ry="2"></rect>
                    <rect x="2" y="14" width="20" height="8" rx="2" ry="2"></rect>
                    <line x1="6" y1="6" x2="6.01" y2="6"></line>
                    <line x1="6" y1="18" x2="6.01" y2="18"></line>
                  </svg>
                  <h3 className="server-form-title">Create New Server</h3>
                </div>
                <div className="server-form-content">
                  <div className="form-field">
                    <label htmlFor="server-name" className="form-label">
                      Server Name <span className="required">*</span>
                    </label>
                    <input
                      id="server-name"
                      type="text"
                      className="form-input"
                      placeholder="e.g., Production Server"
                      value={serverForm.serverName}
                      onChange={(e) => handleServerFormChange('serverName', e.target.value)}
                      disabled={isCreatingServer}
                    />
                  </div>
                  <button
                    className="submit-btn"
                    onClick={handleSaveServer}
                    disabled={isCreatingServer}
                  >
                    <svg className="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                      <polyline points="17 21 17 13 7 13 7 21"></polyline>
                      <polyline points="7 3 7 8 15 8"></polyline>
                    </svg>
                    {isCreatingServer ? 'Creating...' : 'Save Server Configuration'}
                  </button>
                </div>
              </div>
            )}

            <div className="form-submit-section">
              <button
                className="submit-btn primary-submit"
                onClick={handleSubmitForm}
              >
                <svg className="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                Submit Database Configuration
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    </div>
  );
};

export default DatabaseManager;