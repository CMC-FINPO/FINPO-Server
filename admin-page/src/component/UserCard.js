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
import MyInfoModal from './MyInfoModal';

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

export default function UserCard({ fetch, fetchData }) {
  const [users, setUsers] = useState([]);
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);

  const [detailOpen, setDetailOpen] = useState(false);
  const [userDetail, setUserDetail] = useState();

  const [myInfoOpen, setMyInfoOpen] = useState(false);

  useEffect(() => {
    axiosInstance.get('user').then((res) => {
      setUsers([...res.data.data]);
    });
  }, [fetch]);

  useEffect(() => {
    console.log(userDetail);
  }, [userDetail]);

  return (
    <>
      <Card sx={{ minWidth: 200, padding: 1 }}>
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
            <Table sx={{ minWidth: '600px' }} aria-label='simple table'>
              <TableHead>
                <TableRow>
                  <TableCell align='center'>ID</TableCell>
                  <TableCell align='center'>이름</TableCell>
                  <TableCell align='center'>닉네임</TableCell>
                  <TableCell align='center'>기본지역</TableCell>
                  <TableCell align='center'>프로필</TableCell>
                  <TableCell align='center'>소셜타입</TableCell>
                  <TableCell align='center'></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map((user, idx) => (
                  <TableRow
                    key={idx}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                    onClick={(e) => {
                      e.stopPropagation();
                      setDetailOpen(true);
                      setUserDetail(user);
                    }}
                  >
                    <TableCell align='center'>{user.id}</TableCell>
                    <TableCell align='center'>{user.name}</TableCell>
                    <TableCell align='center'>{user.nickname}</TableCell>
                    {/* <TableCell align='center'>{user.gender}</TableCell>
                    <TableCell align='center'>{user.birth}</TableCell>
                    <TableCell align='center'>
                      <div>{user.email.substring(0, 10)}</div>
                      <div>{user.email.substring(10)}</div>
                    </TableCell> */}
                    <TableCell align='center'>{user.defaultRegion?.parent?.name + ' ' + user.defaultRegion?.name}</TableCell>
                    <TableCell align='center'>
                      <Avatar src={user.profileImg} />
                    </TableCell>
                    <TableCell align='center'>{user.oAuthType}</TableCell>
                    <TableCell align='center' width={'20px'}>
                      <Button
                        size='small'
                        variant='contained'
                        color='error'
                        onClick={(e) => {
                          let access_token, code;

                          e.stopPropagation();
                          if (!window.confirm('정말 삭제하시겠습니까?')) return;

                          if (user.oAuthType === 'GOOGLE') {
                            access_token = prompt('구글 access token 입력');
                          }
                          if (user.oAuthType === 'APPLE') {
                            code = prompt('애플 code 입력');
                          }
                          axiosInstance
                            .delete(`user/${user.id}`, {
                              data: {
                                access_token,
                                code,
                              },
                            })
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
          <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, marginTop: 20 }}>
            <Button
              variant='contained'
              sx={{ marginBottom: 0.5 }}
              onClick={(e) => {
                e.stopPropagation();
                setMyInfoOpen(true);
              }}
            >
              내 정보 수정
            </Button>
          </div>
        </Box>
      </Modal>

      <Modal open={detailOpen} onClose={() => setDetailOpen(false)}>
        <Box sx={{ ...style }}>
          {userDetail &&
            Object.entries(userDetail).map(([key, value]) => (
              <div>
                {key}:{value.toString()}
              </div>
            ))}
          <Avatar src={userDetail?.profileImg} />
        </Box>
      </Modal>

      <Modal open={myInfoOpen} onClose={() => setMyInfoOpen(false)}>
        <MyInfoModal fetchData={fetchData} fetch={fetch} />
      </Modal>
    </>
  );
}
