import {useTheme} from "../hooks/UseTheme.tsx";

 function ThemeChangeButton() {
    const { theme, toggleTheme } = useTheme();

    return (
        <button
        className="theme-change-button"
        onClick={toggleTheme}
        >
            Switch to {theme === "light" ? "Dark" : "Light"} Mode
        </button>
    );
}

export default ThemeChangeButton;