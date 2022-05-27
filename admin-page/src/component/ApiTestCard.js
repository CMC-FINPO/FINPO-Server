import { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';

import { axiosInstance } from '../axiosInstance';
import { Button } from '@mui/material';

export default function UserCard() {
  const [method, setMethod] = useState('get');
  const [reqUrl, setReqUrl] = useState('user/me');
  const [reqBody, setReqBody] = useState('{}');
  const [resBody, setResBody] = useState();

  useEffect(() => {
    axiosInstance
      .get('user')
      .then((res) => {})
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

  return (
    <>
      <Card sx={{ minWidth: '600px', padding: 1 }}>
        <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          <Typography sx={{ fontSize: 14 }} color='text.secondary' gutterBottom>
            API Test
          </Typography>

          <Typography variant='h4' component='div'>
            <div style={{ display: 'flex', gap: 7 }}>
              <Select size='small' value={method} onChange={(e) => setMethod(e.target.value)}>
                <MenuItem value={'get'}>GET</MenuItem>
                <MenuItem value={'post'}>POST</MenuItem>
                <MenuItem value={'put'}>PUT</MenuItem>
                <MenuItem value={'delete'}>DELETE</MenuItem>
              </Select>
              <TextField
                fullWidth
                size='small'
                label='request url'
                id='fullWidth'
                value={reqUrl}
                onChange={(e) => {
                  setReqUrl(e.target.value);
                }}
              />
              <Button
                size='sx'
                variant='contained'
                onClick={() => {
                  axiosInstance
                    .request({
                      url: reqUrl,
                      method,
                      data: JSON.parse(reqBody),
                    })
                    .then((res) => {
                      setResBody(JSON.stringify(res.data));
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
                        setResBody(JSON.stringify(res.response.data));
                      }
                    });
                }}
              >
                Send
              </Button>
            </div>
          </Typography>

          <Typography variant='h4' component='div'>
            <TextField
              fullWidth
              size='small'
              label='request body'
              id='fullWidth'
              multiline
              rows={5}
              value={reqBody}
              onChange={(e) => {
                setReqBody(e.target.value);
              }}
            />
          </Typography>

          <Typography variant='h4' component='div'>
            <TextField fullWidth focused size='small' margin='normal' multiline rows={10} label='response body' id='fullWidth' value={resBody} />
          </Typography>
        </CardContent>
      </Card>
    </>
  );
}
