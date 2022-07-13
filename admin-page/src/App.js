import './App.css';
import TopBar from './component/TopBar';
import { Route, Routes, useNavigate, useSearchParams } from 'react-router-dom';
import RegisterPopup from './component/RegisterPopup';
import MainScreen from './screen/MainScreen';
import { useEffect, useState, forwardRef } from 'react';
import jwt_decode from 'jwt-decode';
import CssBaseline from '@mui/material/CssBaseline';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { axiosInstance } from './axiosInstance';
import PolicyScreen from './screen/PolicyScreen';
import { fetchToken, onMessageListener } from './firebase';
import Snackbar from '@mui/material/Snackbar';
import MuiAlert from '@mui/material/Alert';
import PostScreen from './screen/PostScreen';
import ReportScreen from './screen/ReportScreen';
import UserScreen from './screen/UserScreen';

const Alert = forwardRef(function Alert(props, ref) {
  return <MuiAlert elevation={6} ref={ref} variant='filled' {...props} />;
});

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

  const [show, setShow] = useState(false);
  const [notification, setNotification] = useState({ title: '', body: '' });
  const [isTokenFound, setTokenFound] = useState(false);
  fetchToken(setTokenFound);

  onMessageListener()
    .then((payload) => {
      setNotification(payload?.data);
      setShow(true);
      console.log(payload);
    })
    .catch((err) => console.log('failed: ', err));

  useEffect(() => {
    if (searchParams.get('access-token') && searchParams.get('refresh-token')) {
      localStorage.setItem('accessToken', searchParams.get('access-token'));
      localStorage.setItem('refreshToken', searchParams.get('refresh-token'));

      axiosInstance
        .put(
          'notification/me',
          {
            subscribe: true,
            registrationToken: localStorage.getItem('fcm'),
          },
          { headers: { Authorization: `Bearer ${searchParams.get('access-token')}` } }
        )
        .then((res) => {
          console.log('res: ' + res.data.data);
        })
        .finally(() => {
          navigate('/');
        });
    }

    if (localStorage.getItem('accessToken')) setUser(jwt_decode(localStorage.getItem('accessToken')));

    if (localStorage.getItem('accessToken')) {
      console.log('called');

      axiosInstance
        .post('oauth/reissue', {
          accessToken: localStorage.getItem('accessToken'),
          refreshToken: localStorage.getItem('refreshToken'),
        })
        .then((res) => {
          localStorage.setItem('accessToken', res.data.data.accessToken);
          localStorage.setItem('refreshToken', res.data.data.refreshToken);
          setUser(jwt_decode(res.data.data.accessToken));
        });
    }
  }, [fetch]);

  return (
    <ThemeProvider theme={true ? themeLight : themeDark}>
      <CssBaseline />
      <div className='App' style={{}}>
        <TopBar user={user} setUser={setUser} fetch={fetch} fetchData={fetchData} />
        <Snackbar onClose={() => setShow(false)} open={show} autoHideDuration={3000} message={notification.title}>
          <Alert onClose={() => setShow(false)} severity='success' sx={{ width: '100%' }}>
            {'종류: ' + notification.type + ', id: ' + notification.id + ', 제목: ' + notification.title}
          </Alert>
        </Snackbar>
        <Routes>
          <Route path='/' element={<MainScreen fetchData={fetchData} fetch={fetch} user={user} setUser={setUser} />}></Route>
          <Route path='/register/*' element={<RegisterPopup />}></Route>
          <Route path='/policy/*' element={<PolicyScreen fetchData={fetchData} fetch={fetch} user={user} setUser={setUser} />}></Route>
          <Route path='/post/*' element={<PostScreen fetchData={fetchData} fetch={fetch} user={user} setUser={setUser} />}></Route>
          <Route path='/report/*' element={<ReportScreen fetchData={fetchData} fetch={fetch} user={user} setUser={setUser} />}></Route>
          <Route path='/user/*' element={<UserScreen fetchData={fetchData} fetch={fetch} user={user} setUser={setUser} />}></Route>
        </Routes>
      </div>

      {isTokenFound && <h1> Notification permission enabled 👍🏻 </h1>}
      {!isTokenFound && <h1> Need notification permission ❗️ </h1>}
    </ThemeProvider>
  );
}

export default App;
