import LoginPage from "./pages/LoginPage.tsx";
import {useTheme} from "./hooks/UseTheme.tsx";
import { Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage.tsx";
import ProtectedRoute from "./components/ProtectedRoutes.tsx";

function App() {
    const { theme } = useTheme();

  return (
      <div className={`app ${theme}`}>
          <Routes>
              <Route path="/" element={
                  <ProtectedRoute>
                  <HomePage />
                  </ProtectedRoute>
              } />
              <Route path="/login" element={
                      <LoginPage />
              } />
          </Routes>
      </div>
  )
}

export default App
