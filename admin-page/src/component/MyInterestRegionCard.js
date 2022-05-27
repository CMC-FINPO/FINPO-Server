import { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

import { axiosInstance } from '../axiosInstance';

import Modal from '@mui/material/Modal';
import { Avatar, FormControl, InputLabel, MenuItem, Select } from '@mui/material';

const style = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  bgcolor: 'background.paper',
  borderRadius: 4,
  boxShadow: 24,
  p: 4,
  maxWidth: '95vw',
};

export default function MyInterestRegionCard({ fetchData, fetch }) {
  const [users, setUsers] = useState([]);
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);

  const [detailOpen, setDetailOpen] = useState(false);
  const [userDetail, setUserDetail] = useState();

  const [region1, setRegion1] = useState();
  const [region2, setRegion2] = useState();
  const [regions1, setRegions1] = useState([]);
  const [regions2, setRegions2] = useState([]);

  useEffect(() => {
    axiosInstance
      .get('region')
      .then((res) => {
        setUsers([...res.data.data]);
      })
      .catch((res) => {
        if (res.response.data.errorCode === '40001') {
          axiosInstance
            .post('oauth/reissue', {
              accessToken: localStorage.getItem('accessToken'),
              refreshToken: localStorage.getItem('refreshToken'),
            })
            .then((res) => {
              localStorage.setItem('accessToken', res.data.data.accessToken);
              localStorage.setItem('refreshToken', res.data.data.refreshToken);
              alert('토큰이 만료되어 갱신합니다');
              fetchData();
            });
        } else {
          localStorage.clear();
        }
      });
  }, [fetch]);

  useEffect(() => {
    axiosInstance.get('region/name').then((res) => {
      console.log(res);
      setRegions1([...res.data.data]);
    });
  }, []);
  useEffect(() => {
    axiosInstance.get(`region/name?region1=${region1}`).then((res) => {
      setRegions2([...res.data.data]);
    });
  }, [region1]);

  return (
    <>
      <Card sx={{ minWidth: 200, padding: 1 }}>
        <CardContent>
          <Typography sx={{ fontSize: 14 }} color='text.secondary' gutterBottom>
            나의 관심지역
          </Typography>
          <Typography variant='h4' component='div'>
            {users.length}
          </Typography>
          <Typography sx={{}} color='text.secondary'>
            개
          </Typography>
        </CardContent>
        <CardActions sx={{ display: 'flex', justifyContent: 'center' }}>
          <Button size='small' onClick={handleOpen}>
            자세히 보기
          </Button>
        </CardActions>
      </Card>
      <Modal open={open} onClose={handleClose} aria-labelledby='modal-modal-title' aria-describedby='modal-modal-description'>
        <Box sx={style}>
          <TableContainer component={Paper}>
            <Table sx={{ minWidth: '500px' }} aria-label='simple table'>
              <TableHead>
                <TableRow>
                  <TableCell align='center'>ID</TableCell>
                  <TableCell align='center'>기본지역?</TableCell>
                  <TableCell align='center'>지역1</TableCell>
                  <TableCell align='center'>지역2</TableCell>
                  <TableCell align='center'></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map((user, idx) => (
                  <TableRow key={idx} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                    <TableCell align='center'>{user.id}</TableCell>
                    <TableCell align='center'>{user.isDefault ? 'O' : 'X'}</TableCell>
                    <TableCell align='center'>{user.region1}</TableCell>
                    <TableCell align='center'>{user.region2}</TableCell>
                    <TableCell align='center'>
                      <Button
                        size='small'
                        variant='contained'
                        color='error'
                        onClick={(e) => {
                          e.stopPropagation();
                          if (!window.confirm('정말 삭제하시겠습니까?')) return;

                          axiosInstance
                            .delete(`region/${user.id}`)
                            .then(() => {
                              fetchData();
                            })
                            .catch((res) => {
                              alert(res.response.data.message);
                            });
                        }}
                      >
                        삭제
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, marginTop: 30 }}>
            <div style={{ width: '40%', display: 'flex', flexDirection: 'column', gap: 10 }}>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>지역</InputLabel>
                <Select value={region1} onChange={(e) => setRegion1(e.target.value)}>
                  {regions1.map((region, idx) => {
                    return <MenuItem value={region}>{region}</MenuItem>;
                  })}
                </Select>
              </FormControl>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>상세 지역</InputLabel>
                <Select value={region2} onChange={(e) => setRegion2(e.target.value)}>
                  {regions2.map((region, idx) => {
                    return <MenuItem value={region}>{region}</MenuItem>;
                  })}
                </Select>
              </FormControl>
            </div>
            <div style={{ width: '100%', display: 'flex', justifyContent: 'center', gap: 20 }}>
              <Button
                variant='contained'
                sx={{ marginBottom: 0.5 }}
                onClick={(e) => {
                  e.stopPropagation();
                  axiosInstance
                    .post(`region/me`, {
                      region1,
                      region2,
                    })
                    .then(() => {
                      fetchData();
                    })
                    .catch((res) => {
                      alert(res.response.data.message);
                    });
                }}
              >
                관심지역 추가
              </Button>
              <Button
                variant='contained'
                sx={{ marginBottom: 0.5 }}
                onClick={(e) => {
                  e.stopPropagation();
                  axiosInstance
                    .post(`region/my-default`, {
                      region1,
                      region2,
                    })
                    .then(() => {
                      fetchData();
                    })
                    .catch((res) => {
                      alert(res.response.data.message);
                    });
                }}
              >
                기본지역 변경
              </Button>
            </div>
          </div>
        </Box>
      </Modal>
    </>
  );
}
