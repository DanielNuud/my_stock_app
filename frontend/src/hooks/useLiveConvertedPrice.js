import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";

const useLiveConvertedPrice = (ticker, currency) => {
  const [price, setPrice] = useState(null);
  const [timestamp, setTimestamp] = useState(null);

  useEffect(() => {
    if (!ticker || !currency) return;

      const scheme = location.protocol === 'https:' ? 'wss' : 'ws';
      const backendPort = 8080;
      const wsUrl = `${scheme}://${location.hostname}:${backendPort}/ws/stocks`;

      const client = new Client({
          brokerURL: wsUrl,          // теперь всегда тот же хост, что и страница
          reconnectDelay: 5000,
          debug: str => console.log(str),
      });

    client.onConnect = () => {
      console.log("Connected to converted price WebSocket");

      client.subscribe(`/topic/stocks/${ticker}/${currency}`, (message) => {
        const data = JSON.parse(message.body);
        setPrice(data.price);
        setTimestamp(data.timestamp);
      });
    };

    client.activate();

    return () => {
      console.log("Disconnecting converted price WebSocket");
      client.deactivate();
    };
  }, [ticker, currency]);

  return { price, timestamp };
};

export default useLiveConvertedPrice;
