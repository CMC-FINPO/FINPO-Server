import './App.css';
import TopBar from './component/TopBar';
import { BrowserRouter, Route, Routes, useNavigate, useSearchParams } from 'react-router-dom';
import RegisterPopup from './component/RegisterPopup';
import MainScreen from './screen/MainScreen';
import { useEffect, useState } from 'react';
import jwt_decode from 'jwt-decode';
import CssBaseline from '@mui/material/CssBaseline';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import UserCard from './component/UserCard';
import { axiosInstance } from './axiosInstance';

const themeLight = createTheme({
  palette: {
    background: {
      default: '#f8f8f8',
    },
  },
});

const themeDark = createTheme({
  palette: {
    background: {
      default: '#222222',
    },
    text: {
      primary: '#ffffff',
    },
  },
});

function App() {
  const [user, setUser] = useState();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const [fetch, setFetch] = useState(false);
  const fetchData = () => setFetch(!fetch);

  useEffect(() => {
    if (searchParams.get('access-token') && searchParams.get('refresh-token')) {
      localStorage.setItem('accessToken', searchParams.get('access-token'));
      localStorage.setItem('refreshToken', searchParams.get('refresh-token'));
      navigate('/');
      fetchData();
    }
  }, []);

  return (
    <ThemeProvider theme={true ? themeLight : themeDark}>
      <CssBaseline />
      <div className='App' style={{}}>
        <TopBar user={user} setUser={setUser} fetch={fetch} fetchData={fetchData} />
        <Routes>
          <Route path='/' element={<MainScreen fetchData={fetchData} fetch={fetch} user={user} setUser={setUser} />}></Route>
          <Route path='/register/*' element={<RegisterPopup />}></Route>
        </Routes>
      </div>
    </ThemeProvider>
  );
}

export default App;
