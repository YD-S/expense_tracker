import ThemeChangeButton from "../components/ThemeChangeButton.tsx";

function LoginPage(){
    return (
        <>
            <ThemeChangeButton />
            <div className={"bg-bg text-text"}>
            <h1>Login Page</h1>
            <p>Please enter your credentials to log in.</p>
            </div>
        </>
    );
}

export default LoginPage;