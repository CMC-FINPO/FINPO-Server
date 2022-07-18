import { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Switch from '@mui/material/Switch';
import FormControlLabel from '@mui/material/FormControlLabel';
import Divider from '@mui/material/Divider';

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
  minWidth: '400px',
};

export default function MyNotification({ fetchData, fetch }) {
  const [notification, setNotification] = useState();
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);
  const [allNotice, setAllNotice] = useState(false);
  const [categories, setCategories] = useState([]);
  const [regions, setRegions] = useState([]);

  useEffect(() => {
    axiosInstance.get('notification/me').then((res) => {
      setNotification(res.data.data);
      setAllNotice(res.data.data?.subscribe);
      setCategories([...res.data.data?.interestCategories]);
      setRegions([...res.data.data?.interestRegions]);
    });
  }, [fetch]);

  return (
    <>
      <Card sx={{ minWidth: 200, padding: 1 }}>
        <CardContent>
          <Typography sx={{ fontSize: 14 }} color='text.secondary' gutterBottom>
            알림 설정 여부
          </Typography>
          <Typography variant='h4' component='div'>
            {notification?.subscribe ? '켜짐' : '꺼짐'}
          </Typography>
        </CardContent>
        <CardActions sx={{ display: 'flex', justifyContent: 'center' }}>
          <Button size='small' onClick={handleOpen}>
            알림 설정
          </Button>
        </CardActions>
      </Card>
      <Modal open={open} onClose={handleClose} aria-labelledby='modal-modal-title' aria-describedby='modal-modal-description'>
        <Box sx={style}>
          <FormControlLabel
            checked={allNotice}
            onChange={() => setAllNotice(!allNotice)}
            control={<Switch color='primary' />}
            label='전체 알림'
            labelPlacement='start'
            sx={{ display: 'flex', justifyContent: 'center' }}
          />
          <div style={{ display: 'flex', gap: 50 }}>
            <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, marginTop: 30 }}>
              {categories.map((category, idx) => {
                return (
                  <>
                    <FormControlLabel
                      checked={category.subscribe}
                      onChange={() => {
                        categories[idx].subscribe = !category.subscribe;
                        setCategories([...categories]);
                      }}
                      control={<Switch color='primary' />}
                      label={category.category.parent.name + '/' + category.category.name}
                      labelPlacement='start'
                      disabled={!allNotice}
                    />
                  </>
                );
              })}
            </div>
            <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, marginTop: 30 }}>
              {regions.map((region, idx) => {
                return (
                  <>
                    <FormControlLabel
                      checked={region.subscribe}
                      onChange={() => {
                        regions[idx].subscribe = !region.subscribe;
                        setRegions([...regions]);
                      }}
                      control={<Switch color='primary' />}
                      label={region.region.parent.name + '/' + region.region.name}
                      labelPlacement='start'
                      disabled={!allNotice}
                    />
                  </>
                );
              })}
            </div>
          </div>

          <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: 30 }}>
            <b>Registration Token </b>
            {localStorage.getItem('fcm')}
            <Button
              variant='contained'
              sx={{ marginBottom: 0.5 }}
              onClick={(e) => {
                axiosInstance
                  .put(`notification/me`, {
                    subscribe: allNotice,
                    interestCategories: categories,
                    interestRegions: regions,
                    registrationToken: localStorage.getItem('fcm'),
                  })
                  .then(() => {
                    handleClose();
                    fetchData();
                  })
                  .catch((res) => {
                    alert(res.response.data.message);
                  });
              }}
            >
              알림 설정 변경
            </Button>
          </div>
        </Box>
      </Modal>
    </>
  );
}
