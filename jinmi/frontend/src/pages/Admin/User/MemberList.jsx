
import React, { useState, useEffect } from "react";
import apiClient from "../../../api/apiClient.js";
import "./MemberList.css";

export default function MemberList() {
   const [users, setUsers] = useState([]);
   const [loading, setLoading] = useState(true);

   const fetchUsers = async () => {
       try{
           setLoading(true);
           const response = await apiClient.get("/users/all");
           setUsers(response.data.data || []);
       }catch (error) {
           console.error("회원 목록 로드 실패 :" ,error);
           alert("회원 목록을 불러오는 중 오류가 발생하였습니다.");
       } finally {
           setLoading(false);
       }
   };

   useEffect(() => {
       fetchUsers();
   }, []);

   const handleDelete = async (userId, email) => {
       if (window.confirm(`[${email}] 회원을 정말 강제 탈퇴시키겠습니까?`)) {
           try {
               await apiClient.delete(`/users/admin/delete/${userId}`);
               alert("삭제가 완료되었습니다.");
               fetchUsers();
           } catch (error) {
               console.error("삭제 실패:", error);
               alert("삭제 처리에 실패했습니다. 권한을 확인하세요.");
           }
       }
   };

   if (loading) return <div className="admin-loading">데이터를 불러오는 중입니다...</div>

    return (
        <div className="member-list-container">
            <header className="content-header">
                <h2>회원관리</h2>
                <div className="stats-bar">
                    <span className="stats-item">
                        전체 회원수: <strong>{users.length}</strong>명
                    </span>
                </div>
            </header>

            <div className="table-wrapper">
                <table className="admin-table">
                    <thead>
                    <tr>
                        <th>번호</th>
                        <th>이메일(아이디)</th>
                        <th>닉네임</th>
                        <th>가입채널</th>
                        <th>가입일</th>
                        <th>최종접속</th>
                        <th>권한</th>
                        <th>관리</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.length > 0 ? (
                        users.map((user, index) => (
                            <tr key={user.userId || index}>
                                <td>{index + 1}</td>
                                <td className="text-left">{user.email}</td>
                                <td>{user.displayName}</td>
                                <td>
                                        <span className={`provider-badge ${user.provider}`}>
                                            {user.provider || 'local'}
                                        </span>
                                </td>
                                <td>{user.createAt?.split('T')[0] || "-"}</td>
                                <td>{user.lastLoginAt?.split('T')[0] || "-"}</td>
                                <td>
                                        <span className={`role-tag ${user.role}`}>
                                            {user.role}
                                        </span>
                                </td>
                                <td>
                                    <div className="action-buttons">
                                        <button className="btn-edit" onClick={() => alert('수정 기능 준비 중')}>수정</button>
                                        <button
                                            className="btn-delete"
                                            onClick={() => handleDelete(user.userId, user.email)}
                                        >
                                            삭제
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan="8">가입된 회원이 없습니다.</td>
                        </tr>
                    )}
                    </tbody>

                </table>
            </div>
        </div>
    )
}