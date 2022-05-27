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
import { Avatar } from '@mui/material';

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

export default function InterestRegionCard({ fetch, fetchData }) {
  const [users, setUsers] = useState([]);
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);

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
              window.location.reload();
            });
        } else {
          localStorage.clear();
        }
      });
  }, [fetch]);

  return (
    <>
      <Card sx={{ minWidth: 200, padding: 1 }}>
        <CardContent>
          <Typography sx={{ fontSize: 14 }} color='text.secondary' gutterBottom>
            등록된 관심지역
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
                  <TableCell align='center'>유저ID</TableCell>
                  <TableCell align='center'>유저의 기본지역?</TableCell>
                  <TableCell align='center'>지역1</TableCell>
                  <TableCell align='center'>지역2</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map((user, idx) => (
                  <TableRow key={idx} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                    <TableCell align='center'>{user.id}</TableCell>
                    <TableCell align='center'>{user.userId}</TableCell>
                    <TableCell align='center'>{user.isDefault ? 'O' : 'X'}</TableCell>
                    <TableCell align='center'>{user.region1}</TableCell>
                    <TableCell align='center'>{user.region2}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      </Modal>
    </>
  );
}
