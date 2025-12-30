import React, { useState, useEffect } from "react";
import apiClient from "../../../api/apiClient.js";
import "./AccessStats.css"; // CSS 파일 분리

export default function AccessStats() {
    const [logs, setLogs] = useState([]);
    const [filteredLogs, setFilteredLogs] = useState([]);
    const [loading, setLoading] = useState(true);

    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");

    const fetchLogs = async () => {
        try {
            setLoading(true);
            const response = await apiClient.get("/admin/access-logs");
            setLogs(response.data || []);
        } catch (error) {
            console.error("접속 로그 로드 실패:", error);
            alert("접속 로그를 불러오는 중 오류가 발생하였습니다.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs();
    }, []);

    useEffect(() => {
        if (!startDate && !endDate) {
            setFilteredLogs(logs);
        } else {
            const filtered = logs.filter(log => {
                const logDate = log.accessTime?.split('T')[0];

                if (startDate && endDate) {
                    return logDate >= startDate && logDate <= endDate;
                } else if (startDate) {
                    return logDate >= startDate;
                } else if (endDate) {
                    return logDate <= endDate;
                }
                return true;
            });
            setFilteredLogs(filtered);
        }
    }, [startDate, endDate, logs]);

    const getCountryEmoji = (country) => {
        if (country === 'South Korea') return '🇰🇷';
        if (country === 'United States') return 'us';
        if (country === 'China') return 'cn';
        if (country === 'Japan') return 'jp';
        if (country === 'Local') return '🏠';
        return '🏳️';
    };

    const resetFilter = () => {
        setStartDate("");
        setEndDate("");
    };

    if (loading) return <div className="admin-loading">데이터를 불러오는 중입니다...</div>;

    return (
        <div className="member-list-container">
            <header className="content-header">
                <h2>접속자 집계</h2>
                <div className="stats-bar">
                    <div className="period-filter">
                        <input
                            type="date"
                            className="date-picker"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                        <span className="separator">~</span>
                        <input
                            type="date"
                            className="date-picker"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                        />
                        {(startDate || endDate) && (
                            <button onClick={resetFilter} className="btn-reset">초기화</button>
                        )}
                    </div>
                    <span className="stats-item">
                        검색 결과: <strong>{filteredLogs.length}</strong>건
                    </span>
                </div>
            </header>

            <div className="table-wrapper">
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>번호</th>
                        <th>접속 시간</th>
                        <th>IP 주소</th>
                        <th>국가</th>
                        <th>기기 / OS</th>
                        <th>브라우저</th>
                        <th>요청 URL</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredLogs.length > 0 ? (
                        filteredLogs.map((log, index) => (
                            <tr key={log.id || index}>
                                <td>{filteredLogs.length - index}</td>
                                <td>{log.accessTime?.replace('T', ' ').split('.')[0]}</td>
                                <td className="text-left font-mono">{log.ip}</td>
                                <td>
                                    <span className="country-badge">
                                        {getCountryEmoji(log.country)} {log.country}
                                    </span>
                                </td>
                                <td>{log.device} / {log.os}</td>
                                <td><span className="browser-tag">{log.browser}</span></td>
                                <td className="text-left">
                                    <code className="url-code">{log.requestUrl}</code>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="7">해당 날짜에 수집된 접속 로그가 없습니다.</td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}