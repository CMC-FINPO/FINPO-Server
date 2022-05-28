import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Modal from '@mui/material/Modal';
import { styled } from '@mui/material/styles';
import { useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Checkbox, FormControl, FormControlLabel, FormGroup, FormLabel, InputLabel, Radio, RadioGroup, Select, TextField } from '@mui/material';
import MenuItem from '@mui/material/MenuItem';
import { axiosInstance } from '../axiosInstance';
import LoadingButton from '@mui/lab/LoadingButton';

const style = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  width: 400,
  bgcolor: 'background.paper',
  border: '2px solid #000',
  boxShadow: 24,
  p: 4,
  maxWidth: '95vw',
  overflow: 'auto',
  display: 'flex',
  flexDirection: 'column',
  gap: 2,
  justifyContent: 'center',
};

const Input = styled('input')({
  display: 'none',
});

export default function RegisterPopup() {
  const [open, setOpen] = React.useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => {
    setOpen(false);
    navigate('/');
  };

  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const location = useLocation();

  const [name, setName] = useState();
  const [nickname, setNickname] = useState();
  const [birth, setBirth] = useState();
  const [gender, setGender] = useState();
  const [email, setEmail] = useState();
  const [region1, setRegion1] = useState();
  const [region2, setRegion2] = useState();
  const [interestPolicy, setInterestPolicy] = useState([]);
  const [profileImg, setProfileImg] = useState();
  const [profileImgUrl, setProfileImgUrl] = useState();
  const [isNicknameDuplicate, setNicknameDuplicate] = useState(null);

  const [regions1, setRegions1] = useState([]);
  const [regions2, setRegions2] = useState([]);

  const [isRegisterLoading, setRegisterLoading] = useState(false);

  useEffect(() => {
    axiosInstance.get('region/name').then((res) => {
      console.log(res);
      setRegions1([...res.data.data]);
    });

    if (location.pathname.includes('register')) handleOpen();
    setNickname(searchParams.get('nickname'));
    setEmail(searchParams.get('email'));
    setGender(searchParams.get('gender'));
    setName(searchParams.get('name'));
    setBirth(searchParams.get('birth'));
    setProfileImgUrl(searchParams.get('profileImg'));
  }, []);

  useEffect(() => {
    axiosInstance.get(`region/name?region1=${region1}`).then((res) => {
      setRegions2([...res.data.data]);
    });
  }, [region1]);

  return (
    <div>
      <Modal open={open} onClose={handleClose}>
        <Box sx={style}>
          <Typography id='modal-modal-title' variant='h4' component='h2'>
            {location.pathname.slice(location.pathname.lastIndexOf('/') + 1, location.pathname.length)}로 회원가입
          </Typography>
          <TextField size='small' label='이름' variant='outlined' value={name} onChange={(e) => setName(e.target.value)} />
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 10 }}>
            <TextField
              size='small'
              label='닉네임'
              variant='outlined'
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              sx={{ flex: 1 }}
              error={isNicknameDuplicate}
              helperText={isNicknameDuplicate === true ? '닉네임이 중복됩니다' : isNicknameDuplicate === false ? '사용 가능합니다' : ''}
            />
            <Button
              variant='contained'
              size='small'
              sx={{ height: '50px' }}
              onClick={() => {
                axiosInstance
                  .get(`user/check-duplicate?nickname=${nickname}`)
                  .then((res) => {
                    setNicknameDuplicate(res.data.data);
                  })
                  .catch((res) => {
                    console.log(res.response.data);
                  });
              }}
            >
              중복 확인
            </Button>
          </div>
          <TextField size='small' label='생년월일(yyyy-mm-dd)' variant='outlined' value={birth} onChange={(e) => setBirth(e.target.value)} />
          <TextField size='small' label='이메일' variant='outlined' value={email} onChange={(e) => setEmail(e.target.value)} />
          <FormControl>
            <FormLabel id='demo-row-radio-buttons-group-label'>성별</FormLabel>
            <RadioGroup row value={gender} onChange={(e) => setGender(e.target.value)}>
              <FormControlLabel value='MALE' control={<Radio />} label='Male' />
              <FormControlLabel value='FEMALE' control={<Radio />} label='Female' />
              <FormControlLabel value='PRIVATE' control={<Radio />} label='Private' />
            </RadioGroup>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel id='demo-simple-select-label'>지역</InputLabel>
            <Select size='small' value={region1} onChange={(e) => setRegion1(e.target.value)}>
              {regions1.map((region, idx) => {
                return <MenuItem value={region}>{region}</MenuItem>;
              })}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel id='demo-simple-select-label'>상세 지역</InputLabel>
            <Select size='small' value={region2} onChange={(e) => setRegion2(e.target.value)}>
              {regions2.map((region, idx) => {
                return <MenuItem value={region}>{region}</MenuItem>;
              })}
            </Select>
          </FormControl>
          <input
            accept='image/*'
            id='contained-button-file'
            multiple
            type='file'
            onChange={(e) => {
              setProfileImg(e.target.files[0]);
            }}
          />

          {isRegisterLoading ? (
            <LoadingButton variant='outlined' loading>
              Loading...
            </LoadingButton>
          ) : (
            <Button
              variant='outlined'
              loading
              onClick={() => {
                setRegisterLoading(true);

                const formData = new FormData();

                if (profileImg) formData.append('profileImgFile', profileImg);
                if (name) formData.append('name', name);
                if (birth) formData.append('birth', birth);
                if (email) formData.append('email', email);
                if (gender) formData.append('gender', gender);
                if (region2) formData.append('region1', region1);
                if (region2) formData.append('region2', region2);
                if (nickname) formData.append('nickname', nickname);
                if (profileImgUrl) formData.append('profileImg', profileImgUrl);

                axiosInstance
                  .post(`oauth/register/${location.pathname.slice(location.pathname.lastIndexOf('/') + 1, location.pathname.length)}`, formData, {
                    headers: {
                      Authorization: `Bearer ${searchParams.get('token')}`,
                      'Content-Type': 'multipart/form-data',
                    },
                  })
                  .then((res) => {
                    localStorage.setItem('accessToken', res.data.data.accessToken);
                    localStorage.setItem('refreshToken', res.data.data.refreshToken);
                    handleClose();
                    window.location.reload();
                  })
                  .catch((res) => {
                    alert(res.response.data.message);
                  })
                  .finally(() => {
                    setRegisterLoading(false);
                  });
              }}
            >
              가입하기
            </Button>
          )}
        </Box>
      </Modal>
    </div>
  );
}
