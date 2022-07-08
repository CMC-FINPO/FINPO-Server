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
import { useNavigate } from 'react-router-dom';

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

export default function PostCard({ fetchData, fetch }) {
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);
  const navigate = useNavigate();

  const [length, setLength] = useState(0);

  useEffect(() => {
    axiosInstance.get('post/search?page=0&size=1').then((res) => {
      setLength(res.data.data.totalElements);
    });
  }, [fetch]);

  return (
    <>
      <Card sx={{ minWidth: 200, padding: 1 }}>
        <CardContent>
          <Typography sx={{ fontSize: 14 }} color='text.secondary' gutterBottom>
            등록된 게시글
          </Typography>
          <Typography variant='h4' component='div'>
            {length}
          </Typography>
          <Typography sx={{}} color='text.secondary'>
            개
          </Typography>
        </CardContent>
        <CardActions sx={{ display: 'flex', justifyContent: 'center' }}>
          <Button size='small' onClick={() => navigate('/post')}>
            자세히 보기
          </Button>
        </CardActions>
      </Card>
    </>
  );
}
