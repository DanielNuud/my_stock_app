import { useEffect } from "react";
import { Client } from "@stomp/stompjs";

const useLiveStockChartData = (ticker, period, setHistoricalData) => {
    useEffect(() => {
        if (period !== "today") {
            console.log("Not in live mode, skipping WebSocket connection.");
            return; 
        }

        const client = new Client({
            brokerURL: "ws://localhost:8080/ws/stocks",
            reconnectDelay: 5000,
            debug: (str) => console.log(str),
        });

        client.onConnect = () => {
            console.log("Connected to live WebSocket");

            client.subscribe(`/topic/stocks/${ticker}`, (message) => {
                const newData = JSON.parse(message.body);

                setHistoricalData(prevData => [
                    ...prevData,
                    {
                        id: { date: new Date(newData.timestamp).toLocaleTimeString() },
                        closePrice: newData.price,
                    },
                ]);
            });
        };

        client.activate();

        return () => {
            console.log("Deactivating WebSocket on period change.");
            client.deactivate();
        };
    }, [ticker, period, setHistoricalData]);
};

export default useLiveStockChartData;
