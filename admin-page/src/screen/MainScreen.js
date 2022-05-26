import * as React from 'react';
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import UserCard from '../component/UserCard';

export default function MainScreen({ user, setUser }) {
  return <div style={{ padding: 40 }}>{!user ? <div>인증된 사용자만 확인할 수 있습니다</div> : <UserCard></UserCard>}</div>;
}
