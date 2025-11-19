import LogIn from './pages/login/LogIn.jsx';
import SignUp from './pages/signup/SignUp.jsx';
import OTPInput from './pages/login/otp/OTPInput.jsx';
import Reset from './pages/login/reset/Reset.jsx';
import Recovered from './pages/login/Recovered/Recovered.jsx';
import Diagram from './pages/diagrams/Diagram.jsx';

export const routes = [
	{
		path: '/login',
		element: <LogIn />
	},
	{
		path: '/signup',
		element: <SignUp />
	},
	{
		path: '/otp',
		element: <OTPInput />
	},
	{
		path: '/reset',
		element: <Reset />
	},
	{
		path: '/recovered',
		element: <Recovered />
	},
	{
		path: '/diagrams',
		element: <Diagram />
	}
]