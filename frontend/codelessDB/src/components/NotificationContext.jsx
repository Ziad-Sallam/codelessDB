import { createContext, useContext, useState } from 'react';
import Notification from '../components/Notification.jsx';

const NotificationContext = createContext();

export function useNotification() {
	const context = useContext(NotificationContext);
	if (!context) {
		throw new Error('useNotification must be used within NotificationProvider');
	}
	return context;
}

export function NotificationProvider({ children }) {
	const [notification, setNotification] = useState({
		open: false,
		message: '',
		severity: 'success',
		duration: 3000
	});

	function showNotification(message, severity = 'success', duration = 3000) {
		setNotification({
			open: true,
			message,
			severity,
			duration
		});
	}

	function showSuccess(message, duration = 3000) {
		showNotification(message, 'success', duration);
	}

	function showError(message, duration = 3000) {
		showNotification(message, 'error', duration);
	}

	function showWarning(message, duration = 3000) {
		showNotification(message, 'warning', duration);
	}

	function showInfo(message, duration = 3000) {
		showNotification(message, 'info', duration);
	}

	function hideNotification() {
		setNotification(prev => ({ ...prev, open: false }));
	}

	return (
		<NotificationContext.Provider
			value={{
				showNotification,
				showSuccess,
				showError,
				showWarning,
				showInfo,
				hideNotification
			}}
		>
			{children}
			<Notification
				message={notification.message}
				severity={notification.severity}
				duration={notification.duration}
				open={notification.open}
				onClose={hideNotification}
			/>
		</NotificationContext.Provider>
	);
}