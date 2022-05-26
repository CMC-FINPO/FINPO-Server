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

  display: 'flex',
  flexDirection: 'column',
  gap: 3,
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

  const [regions1, setRegions1] = useState([]);
  const [regions2, setRegions2] = useState([]);

  useEffect(() => {
    axiosInstance.get('region').then((res) => {
      console.log(res);
      setRegions1([...res.data.data]);
    });

    if (location.pathname.includes('register')) handleOpen();
    setNickname(searchParams.get('name'));
    setEmail(searchParams.get('email'));
    setGender(searchParams.get('gender'));
  }, []);

  useEffect(() => {
    axiosInstance.get(`region?region1=${region1}`).then((res) => {
      setRegions2([...res.data.data]);
    });
  }, [region1]);

  return (
    <div>
      <Modal open={open} onClose={handleClose}>
        <Box sx={style}>
          <Typography id='modal-modal-title' variant='h4' component='h2'>
            카카오로 회원가입
          </Typography>

          <TextField label='이름' variant='outlined' value={name} onChange={(e) => setName(e.target.value)} />
          <TextField label='닉네임' variant='outlined' value={nickname} onChange={(e) => setNickname(e.target.value)} />
          <TextField label='생년월일(yyyy-mm-dd)' variant='outlined' value={birth} onChange={(e) => setBirth(e.target.value)} />
          <TextField label='이메일' variant='outlined' value={email} onChange={(e) => setEmail(e.target.value)} />
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
            <Select value={region1} onChange={(e) => setRegion1(e.target.value)}>
              {regions1.map((region, idx) => {
                return <MenuItem value={region}>{region}</MenuItem>;
              })}
            </Select>
          </FormControl>
          <FormControl fullWidth>
            <InputLabel id='demo-simple-select-label'>상세 지역</InputLabel>
            <Select value={region2} onChange={(e) => setRegion2(e.target.value)}>
              {regions2.map((region, idx) => {
                return <MenuItem value={region}>{region}</MenuItem>;
              })}
            </Select>
          </FormControl>

          <label htmlFor='contained-button-file'>
            <Input
              accept='image/*'
              id='contained-button-file'
              multiple
              type='file'
              onChange={(e) => {
                console.log(e.target.files[0]);
                setProfileImg(e.target.files[0]);
              }}
            />
            <Button variant='contained' component='span'>
              프로필사진 업로드
            </Button>
          </label>
          {/*<FormControl>*/}
          {/*    <FormLabel id="demo-row-radio-buttons-group-label">관심 분야</FormLabel>*/}
          {/*    <FormGroup>*/}
          {/*        <FormControlLabel control={<Checkbox/>} label="진로"/>*/}
          {/*        <FormControlLabel control={<Checkbox/>} label="창업"/>*/}
          {/*        ...*/}
          {/*    </FormGroup>*/}
          {/*</FormControl>*/}

          <Button
            onClick={async () => {
              const formData = new FormData();
              if (profileImg) formData.append('profileImgFile', profileImg);
              formData.append('name', name);
              if (birth) formData.append('birth', birth);
              formData.append('email', email);
              formData.append('gender', gender);
              formData.append('region1', region1);
              formData.append('region2', region2);
              formData.append('nickname', nickname);

              const res = await axiosInstance.post(
                `/oauth/register/${location.pathname.slice(location.pathname.lastIndexOf('/') + 1, location.pathname.length)}`,
                formData,
                {
                  headers: {
                    Authorization: `Bearer ${searchParams.get('kakao-token')}`,
                    'Content-Type': 'multipart/form-data',
                  },
                }
              );
              localStorage.setItem('accessToken', res.data.data.accessToken);
              localStorage.setItem('refreshToken', res.data.data.refreshToken);
              handleClose();
              window.location.reload();
            }}
          >
            가입하기
          </Button>
        </Box>
      </Modal>
    </div>
  );
}
