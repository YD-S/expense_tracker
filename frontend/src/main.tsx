import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { ThemeProvider } from "./Provider/ThemeProvider.tsx";
import { BrowserRouter } from "react-router-dom";
import {AuthProvider} from "./Provider/AuthProvider.tsx";

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <BrowserRouter>
          <AuthProvider>
              <ThemeProvider>
                  <App />
              </ThemeProvider>
          </AuthProvider>
      </BrowserRouter>
  </StrictMode>
)
