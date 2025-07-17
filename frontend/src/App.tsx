import LoginPage from "./pages/LoginPage.tsx";
import {useTheme} from "./hooks/UseTheme.tsx";

function App() {
    const { theme } = useTheme();

  return (
      <div className={`app ${theme}`}>
        <LoginPage />
      </div>
  )
}

export default App
