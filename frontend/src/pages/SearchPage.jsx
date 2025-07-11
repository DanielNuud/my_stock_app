import SearchBar from "../components/SearchBar.jsx";

const SearchPage = () => {
    return (
        <div className="search-page">
            <h1 className="search-title">Find Your Stocks Instantly</h1>
            <p className="search-subtitle">
                Enter a ticker to track live prices, view charts, and analyze company details.
            </p>
            <div className="search-bar-wrapper">
                <SearchBar />
            </div>
        </div>
    );
};

export default SearchPage;
