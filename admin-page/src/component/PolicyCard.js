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
import { useNavigate } from 'react-router-dom';

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

export default function ({ fetchData, fetch }) {
  const navigate = useNavigate();

  const [data, setData] = useState([]);
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);

  const [detailOpen, setDetailOpen] = useState(false);
  const [userDetail, setUserDetail] = useState();

  useEffect(() => {
    axiosInstance.get('policy/search?page=0&size=1').then((res) => {
      console.log(res.data.data);
      setData(res.data.data.totalElements);
    });
  }, [fetch]);

  return (
    <>
      <Card sx={{ minWidth: 200, padding: 1 }}>
        <CardContent>
          <Typography sx={{ fontSize: 14 }} color='text.secondary' gutterBottom>
            등록된 청년 정책
          </Typography>
          <Typography variant='h4' component='div'>
            {data}
          </Typography>
          <Typography sx={{}} color='text.secondary'>
            개
          </Typography>
        </CardContent>
        <CardActions sx={{ display: 'flex', justifyContent: 'center' }}>
          <Button size='small' onClick={() => navigate('/policy')}>
            자세히 보기
          </Button>
        </CardActions>
      </Card>
    </>
  );
}
