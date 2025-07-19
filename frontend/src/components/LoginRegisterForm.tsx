import { useState } from 'react';
import { ChevronDown } from 'lucide-react';

function LoginRegisterForm() {
    const [isRegisterOpen, setIsRegisterOpen] = useState(false);
    const [loginData, setLoginData] = useState({ email: '', password: '' });
    const [registerData, setRegisterData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        agreeTerms: false
    });

    const toggleRegister = () => {
        setIsRegisterOpen(!isRegisterOpen);
    };

    const handleLoginSubmit = (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        console.log('Login:', loginData);
    };

    const handleRegisterSubmit = (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        console.log('Register:', registerData);
    };

    return (
        <div className="flex justify-center items-center min-h-screen bg-bg text-text p-4">
            <div className="rounded-[30px] shadow-[0_10px_40px_rgba(0,0,0,0.15)] relative overflow-hidden w-full max-w-[500px] bg-bg-light">
                <div className={`overflow-hidden transition-all duration-700 ease-in-out ${!isRegisterOpen ? 'max-h-[400px] opacity-100 translate-y-0' : 'max-h-0 opacity-0 -translate-y-full'}`}>
                    <div className="p-8 bg-bg-light transform transition-transform duration-700 ease-in-out">
                        <div className="space-y-4">
                            <h1 className="text-4xl font-bold text-center mb-6 text-text">Sign In</h1>
                            <input
                                type="email"
                                placeholder="Email"
                                value={loginData.email}
                                onChange={(e) => setLoginData({...loginData, email: e.target.value})}
                                className="w-full p-4 rounded-[15px] bg-bg border border-border text-text placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-primary transition-all duration-300"
                            />
                            <input
                                type="password"
                                placeholder="Password"
                                value={loginData.password}
                                onChange={(e) => setLoginData({...loginData, password: e.target.value})}
                                className="w-full p-4 rounded-[15px] bg-bg border border-border text-text placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-primary transition-all duration-300"
                            />
                            <button
                                onClick={handleLoginSubmit}
                                className="w-full p-4 text-lg bg-primary text-bg rounded-[15px] font-semibold transition-all duration-300 hover:bg-secondary hover:shadow-lg transform hover:-translate-y-1"
                            >
                                Sign In
                            </button>
                            <div className="text-center">
                                <a href="#" className="text-primary hover:text-secondary transition-colors duration-300">Forgot password?</a>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="relative">
                    <button
                        onClick={toggleRegister}
                        className="w-full p-4 bg-border hover:bg-border-muted transition-all duration-500 flex items-center justify-center space-x-2 text-text font-medium"
                    >
                        <span>{isRegisterOpen ? 'Back to Sign In' : 'Create Account'}</span>
                        <div className={`transform transition-transform duration-500 ${isRegisterOpen ? 'rotate-180' : ''}`}>
                            <ChevronDown size={20} />
                        </div>
                    </button>
                </div>

                <div className={`overflow-hidden transition-all duration-700 ease-in-out ${isRegisterOpen ? 'max-h-[600px] opacity-100 translate-y-0' : 'max-h-0 opacity-0 translate-y-full'}`}>
                    <div className="p-8 bg-bg-dark transform transition-transform duration-700 ease-in-out">
                        <div className="space-y-4">
                            <h1 className="text-4xl font-bold text-center mb-6 text-text">Sign Up</h1>
                            <input
                                type="text"
                                placeholder="Username"
                                value={registerData.username}
                                onChange={(e) => setRegisterData({...registerData, username: e.target.value})}
                                className="w-full p-4 rounded-[15px] bg-bg border border-border text-text placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-secondary transition-all duration-300"
                            />
                            <input
                                type="email"
                                placeholder="Email"
                                value={registerData.email}
                                onChange={(e) => setRegisterData({...registerData, email: e.target.value})}
                                className="w-full p-4 rounded-[15px] bg-bg border border-border text-text placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-secondary transition-all duration-300"
                            />
                            <input
                                type="password"
                                placeholder="Password"
                                value={registerData.password}
                                onChange={(e) => setRegisterData({...registerData, password: e.target.value})}
                                className="w-full p-4 rounded-[15px] bg-bg border border-border text-text placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-secondary transition-all duration-300"
                            />
                            <input
                                type="password"
                                placeholder="Confirm Password"
                                value={registerData.confirmPassword}
                                onChange={(e) => setRegisterData({...registerData, confirmPassword: e.target.value})}
                                className="w-full p-4 rounded-[15px] bg-bg border border-border text-text placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-secondary transition-all duration-300"
                            />
                            <button
                                onClick={handleRegisterSubmit}
                                className="w-full p-4 text-lg bg-secondary text-bg rounded-[15px] font-semibold transition-all duration-300 hover:bg-primary hover:shadow-lg transform hover:-translate-y-1"
                            >
                                Sign Up
                            </button>
                            <div className="flex items-start space-x-3 pt-2">
                                <input
                                    type="checkbox"
                                    id="terms"
                                    checked={registerData.agreeTerms}
                                    onChange={(e) => setRegisterData({...registerData, agreeTerms: e.target.checked})}
                                    className="mt-1 h-4 w-4 text-secondary bg-bg border border-border rounded focus:ring-secondary focus:ring-2"
                                />
                                <label htmlFor="terms" className="text-sm text-text-muted leading-5">
                                    I agree to the{' '}
                                    <a href="#" className="text-secondary hover:text-primary transition-colors duration-300">Terms of Service</a>
                                    {' '}and{' '}
                                    <a href="#" className="text-secondary hover:text-primary transition-colors duration-300">Privacy Policy</a>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default LoginRegisterForm;