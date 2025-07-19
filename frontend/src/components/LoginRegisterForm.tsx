
function LoginRegisterForm () {
    return (
        <div className="flex justify-center items-center h-screen bg-bg text-text">
            <div className="rounded-[30px] shadow-[0_5px_15px_rgba(0,0,0,0.35)] relative overflow-hidden w-[618px] h-[700px] max-w-full min-h-[480px] p-[30px] items-center">
                <div className="form-container sign-up">
                    <form className={"bg-bg-light text-text-light rounded-[30px]"}>
                        <h1 className="flex justify-center text-4xl p-[10px]">Sign Up</h1>
                        <input type="text" placeholder="Name" required={true} className="w-full max-w-[calc(100%-60px)] my-[10px] mx-[30px] p-[5px] rounded-[10px] text-text"/>
                        <input type="email" placeholder="Email" required={true} className="w-full max-w-[calc(100%-60px)] my-[10px] mx-[30px] p-[5px] rounded-[10px] text-text"/>
                        <input type="password" placeholder="Password" required={true} className="w-full max-w-[calc(100%-60px)] my-[10px] mx-[30px] p-[5px] rounded-[10px] text-text"/>
                        <input type="password" placeholder="Confirm Password" required={true} className="w-full max-w-[calc(100%-60px)] my-[10px] mx-[30px] p-[5px] rounded-[10px] text-text"/>
                        <button type="submit" className="w-full max-w-[calc(100%-60px)] my-[10px] mx-[30px] p-[5px] text-l bg-primary rounded-[10px] p-[5px] transition-colors duration-250 hover:bg-bg hover:">Sign Up</button>
                        <label className="flex items-center justify-center text-text-light p-[10px]">
                            <input type="checkbox" className="p-[10px] m-[10px]"/>
                            <span className="checkmark"></span>
                            I agree to the <a href="#">Terms of Service</a> and <a href="#">Privacy Policy</a>
                        </label>
                    </form>
                </div>
                <div className="form-container sign-in">
                    <form className="bg-border text-text-light rounded-[30px]">
                        <h1 className="flex justify-center text-4xl p-[10px]">Sign In</h1>
                        <input type="email" placeholder="Email" className="w-full max-w-[calc(100%-60px)] my-[10px] mx-[30px] p-[5px] rounded-[10px]"></input>
                        <input type="password" placeholder="Password" required={true} id="password" className="w-full max-w-[calc(100%-60px)] my-[10px] mx-[30px] p-[5px] rounded-[10px] text-text"/>
                        <button type="submit" className="w-full max-w-[calc(100%-60px)] my-[10px] mx-[30px] p-[5px] text-l bg-primary rounded-[10px] p-[5px] transition-colors duration-250 hover:bg-bg     text-text">Sign In</button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default LoginRegisterForm;