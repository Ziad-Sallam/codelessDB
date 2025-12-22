import './ConfirmationModal.css';

function ConfirmationModal({
  isOpen,
  onConfirm,
  onCancel,
  title = "Confirm Action",
  message = "Are you sure you want to proceed?",
  confirmText = "Confirm",
  cancelText = "Cancel",
  confirmButtonStyle = "danger" // "danger" or "primary"
}) {

  if (!isOpen) return null;

  return (
    <div className="confirmation-modal-overlay" onClick={onCancel}>
      <div className="confirmation-modal-container" onClick={(e) => e.stopPropagation()}>
        <div className="confirmation-modal-header">
          <h3>{title}</h3>
          <button className="confirmation-modal-close" onClick={onCancel}>✕</button>
        </div>

        <div className="confirmation-modal-body">
          <p>{message}</p>
        </div>

        <div className="confirmation-modal-footer">
          <button
            className="confirmation-modal-btn cancel-btn"
            onClick={onCancel}
          >
            {cancelText}
          </button>
          <button
            className={`confirmation-modal-btn confirm-btn ${confirmButtonStyle}`}
            onClick={onConfirm}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmationModal;