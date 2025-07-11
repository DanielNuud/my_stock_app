import React from "react";
import { Routes, Route } from "react-router-dom";
import SearchPage from "./pages/SearchPage.jsx";
import CompanyPage from "./pages/CompanyPage.jsx";

const App = () => {
    return (
        <Routes>
            <Route path="/" element={<SearchPage />} />
            <Route path="/company/:ticker" element={<CompanyPage />} />
        </Routes>
    );
};

export default App;
