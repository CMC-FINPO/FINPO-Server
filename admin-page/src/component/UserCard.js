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
};

export default function UserCard() {
  const [users, setUsers] = useState([]);
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);

  useEffect(() => {
    axiosInstance
      .get('user')
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
  }, []);

  function createData(name, calories, fat, carbs, protein) {
    return { name, calories, fat, carbs, protein };
  }

  const rows = [
    createData('Frozen yoghurt', 159, 6.0, 24, 4.0),
    createData('Ice cream sandwich', 237, 9.0, 37, 4.3),
    createData('Eclair', 262, 16.0, 24, 6.0),
    createData('Cupcake', 305, 3.7, 67, 4.3),
    createData('Gingerbread', 356, 16.0, 49, 3.9),
  ];

  return (
    <>
      <Card sx={{ maxWidth: 275, padding: 1 }}>
        <CardContent>
          <Typography sx={{ fontSize: 14 }} color='text.secondary' gutterBottom>
            가입한 회원
          </Typography>
          <Typography variant='h4' component='div'>
            {users.length}
          </Typography>
          <Typography sx={{}} color='text.secondary'>
            명
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
            <Table sx={{ minWidth: 1000 }} aria-label='simple table'>
              <TableHead>
                <TableRow>
                  <TableCell align='center'>ID</TableCell>
                  <TableCell align='center'>이름</TableCell>
                  <TableCell align='center'>닉네임</TableCell>
                  <TableCell align='center'>성별</TableCell>
                  <TableCell align='center'>생년월일</TableCell>
                  <TableCell align='center'>이메일</TableCell>
                  <TableCell align='center'>지역1</TableCell>
                  <TableCell align='center'>지역2</TableCell>
                  <TableCell align='center'>프로필사진</TableCell>
                  <TableCell align='center'>소셜로그인 정보</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map((user, idx) => (
                  <TableRow key={idx} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                    <TableCell align='center'>{user.id}</TableCell>
                    <TableCell align='center'>{user.name}</TableCell>
                    <TableCell align='center'>{user.nickname}</TableCell>
                    <TableCell align='center'>{user.gender}</TableCell>
                    <TableCell align='center'>{user.birth}</TableCell>
                    <TableCell align='center'>{user.email}</TableCell>
                    <TableCell align='center'>{user.region1}</TableCell>
                    <TableCell align='center'>{user.region2}</TableCell>
                    <TableCell align='center'>
                      <Avatar src={user.profileImg} />
                    </TableCell>
                    <TableCell align='center'>{user.oAuthType}</TableCell>
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
