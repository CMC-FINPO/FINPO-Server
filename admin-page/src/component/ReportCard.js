import { useEffect, useState } from 'react';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';

import { axiosInstance } from '../axiosInstance';
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

export default function ReportCard({ fetchData, fetch }) {
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);
  const navigate = useNavigate();

  const [length, setLength] = useState();

  useEffect(() => {
    axiosInstance
      .get('report/community?page=0&size=1')
      .then((res) => {
        setLength(res.data.data.totalElements);
      })
      .catch((err) => {
        console.log(err);
        setLength('관리자아님');
      });
  }, [fetch]);

  return (
    <>
      <Card sx={{ minWidth: 200, padding: 1 }}>
        <CardContent>
          <Typography sx={{ fontSize: 14 }} color='text.secondary' gutterBottom>
            신고건수
          </Typography>
          <Typography variant='h4' component='div'>
            {length}
          </Typography>
          <Typography sx={{}} color='text.secondary'>
            개
          </Typography>
        </CardContent>
        <CardActions sx={{ display: 'flex', justifyContent: 'center' }}>
          <Button size='small' onClick={() => navigate('/report')}>
            자세히 보기
          </Button>
        </CardActions>
      </Card>
    </>
  );
}
