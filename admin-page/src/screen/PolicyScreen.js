import * as React from 'react';
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import UserCard from '../component/UserCard';
import ApiTestCard from '../component/ApiTestCard';
import Grid from '@mui/material/Grid';
import InterestRegionCard from '../component/InterestRegionCard';
import MyInterestRegionCard from '../component/MyInterestRegionCard';
import MyInterestCategoryCard from '../component/MyInterestCategoryCard';
import PolicyCard from '../component/PolicyCard';
import Pagination from '@mui/material/Pagination';
import Chip from '@mui/material/Chip';
import TextField from '@mui/material/TextField';

import {
  Button,
  FormControl,
  Input,
  InputLabel,
  MenuItem,
  Modal,
  Paper,
  Select,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Collapse,
} from '@mui/material';
import { axiosInstance } from '../axiosInstance';
import { Box } from '@mui/system';

export default function PolicyScreen({ user, setUser, fetch, fetchData }) {
  const [data, setData] = useState([]);
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);

  const [detailOpen, setDetailOpen] = useState(false);
  const [userDetail, setUserDetail] = useState({});

  const [page, setPage] = useState(1);

  const [text, setText] = useState('');
  const [region, setRegion] = useState([]);
  const [category, setCategory] = useState([]);

  const [title, setTitle] = useState('');
  const [institution, setInstitution] = useState('');
  const [content, setContent] = useState('');

  const [pregion, setPregion] = useState();
  const [pcategory, setPcategory] = useState();

  const [r1, sr1] = useState();
  const [r2, sr2] = useState();
  const [c1, sc1] = useState();
  const [c2, sc2] = useState();

  useEffect(() => {
    let reg = '';
    region.map((e) => (reg += e.region.id + ','));

    let cat = '';
    category.map((e) => (cat += e.category.id + ','));

    axiosInstance.get(`policy/admin?title=${text}&region=${reg}&category=${cat}&page=${page - 1}&size=20`).then((res) => {
      setData({ ...res.data.data });
    });
  }, [page, reload, fetch]);

  useEffect(() => {
    sc1(userDetail?.category?.parent?.id);
    sc2(userDetail?.category?.id);

    sr1(userDetail?.region?.parent?.id);
    sr2(userDetail?.region?.id);
  }, [userDetail]);

  const [region1, setRegion1] = useState();
  const [region2, setRegion2] = useState();
  const [regions1, setRegions1] = useState([]);
  const [regions2, setRegions2] = useState([]);

  useEffect(() => {
    // axiosInstance.get(`policy/category/me`).then((res) => {
    //   console.log(res.data.data);
    //   setCategory([...res.data.data]);
    //   axiosInstance.get(`region/me`).then((res) => {
    //     console.log(res.data.data);
    //     setRegion([...res.data.data]);
    //     reloadTrigger();
    //   });
    // });

    axiosInstance.get('region/name').then((res) => {
      console.log(res);
      setRegions1([...res.data.data]);
    });
  }, []);

  useEffect(() => {
    axiosInstance.get(`region/name?parentId=${region1}`).then((res) => {
      setRegions2([...res.data.data]);
    });
  }, [region1]);

  useEffect(() => {
    axiosInstance.get(`region/name?parentId=${r1}`).then((res) => {
      setRegions2([...res.data.data]);
    });
  }, [r1]);

  const [ca1, setCa1] = useState();
  const [ca2, setCa2] = useState();
  const [cas1, setCas1] = useState([]);
  const [cas2, setCas2] = useState([]);
  useEffect(() => {
    axiosInstance.get('policy/category/name').then((res) => {
      setCas1([...res.data.data]);
    });
  }, []);
  useEffect(() => {
    axiosInstance.get(`policy/category/name?parentId=${ca1}`).then((res) => {
      setCas2([...res.data.data]);
    });
  }, [ca1]);
  useEffect(() => {
    axiosInstance.get(`policy/category/name?parentId=${c1}`).then((res) => {
      setCas2([...res.data.data]);
    });
  }, [c1]);

  const updatePolicy = (e, row, sendNotification) => {
    e.stopPropagation();
    let body = {
      ...userDetail,
      ...(r2 ? { region: { id: r2 } } : r1 !== null && { region: { id: r1 } }),
      ...(c2 && { category: { id: c2 } }),
    };
    delete body.status;
    if (sendNotification === true) body.status = true;
    axiosInstance
      .put(`policy/${row.id}?sendNotification=${sendNotification}`, body)
      .then((res) => {
        if (sendNotification) alert('send to ' + res.data.data);
        reloadTrigger();
      })
      .catch((res) => {
        alert(res.response.data.message);
      });
  };

  return (
    <div style={{ padding: 10, display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'center' }}>
      {!user ? (
        <div>인증된 사용자만 확인할 수 있습니다</div>
      ) : (
        <>
          <div>
            <Input
              variant='filled'
              label='제목 검색'
              value={text}
              onChange={(e) => {
                setText(e.target.value);
              }}
            ></Input>

            <Button variant='contained' onClick={() => reloadTrigger()}>
              검색
            </Button>

            <Button variant='contained' onClick={() => setOpen(true)}>
              정책 추가
            </Button>
          </div>
          <div style={{ width: '100%', display: 'flex', justifyContent: 'space-around' }}>
            <div>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>상위카테</InputLabel>
                <Select size='small' sx={{ width: 100 }} value={ca1} onChange={(e) => setCa1(e.target.value)}>
                  {cas1.map((region, idx) => {
                    return <MenuItem value={region.id}>{region.name}</MenuItem>;
                  })}
                </Select>
              </FormControl>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>하위카테</InputLabel>
                <Select
                  size='small'
                  sx={{ width: 100 }}
                  value={ca2}
                  onChange={(e) => {
                    setCategory([...category, { category: e.target.value }]);
                  }}
                >
                  {cas2.map((region, idx) => {
                    return <MenuItem value={region}>{region.name}</MenuItem>;
                  })}
                </Select>
              </FormControl>
              <div style={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                {category?.map((e, idx) => (
                  <Chip
                    label={e?.category?.parent?.name + '/' + e?.category?.name}
                    onDelete={() => setCategory([...category.splice(idx + 1, 1)])}
                  ></Chip>
                ))}
              </div>
            </div>

            <div>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>지역</InputLabel>
                <Select size='small' sx={{ width: 100 }} value={region1} onChange={(e) => setRegion1(e.target.value)}>
                  {regions1.map((region, idx) => {
                    return <MenuItem value={region.id}>{region.name}</MenuItem>;
                  })}
                </Select>
              </FormControl>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>상세 지역</InputLabel>
                <Select
                  size='small'
                  sx={{ width: 100 }}
                  value={region2}
                  onChange={(e) => {
                    setRegion([...region, { region: e.target.value }]);
                  }}
                >
                  {regions2.map((region, idx) => {
                    return <MenuItem value={region}>{region.name}</MenuItem>;
                  })}
                </Select>
              </FormControl>
              <div style={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                {region?.map((e, idx) => (
                  <Chip
                    label={e?.region?.parent?.name + '/' + e?.region?.name}
                    onDelete={() => {
                      let temp = [...region];
                      temp.splice(idx, 1);
                      setRegion([...temp]);
                    }}
                  ></Chip>
                ))}
              </div>
            </div>
          </div>
          <TableContainer component={Paper}>
            <Table sx={{ minWidth: '600px' }} aria-label='simple table'>
              <TableHead>
                <TableRow>
                  <TableCell align='center'>ID</TableCell>
                  <TableCell align='center'>제목</TableCell>
                  <TableCell align='center'>주관기관</TableCell>
                  <TableCell align='center'>지역</TableCell>
                  <TableCell align='center'>등록일</TableCell>
                  <TableCell align='center'>보이기</TableCell>
                </TableRow>
              </TableHead>
              <TableBody style={{ width: '100%' }}>
                {data?.content?.map((row, idx) => (
                  <>
                    <TableRow
                      key={idx}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      onClick={(e) => {
                        e.stopPropagation();
                        if (row && userDetail && userDetail.id === row.id) {
                          let temp = { ...userDetail };
                          temp.id = -1;
                          setUserDetail(temp);
                        } else {
                          axiosInstance.get(`policy/${row.id}`).then((res) => {
                            setUserDetail(res.data.data);
                          });
                        }
                      }}
                    >
                      <TableCell align='center'>{row.id}</TableCell>
                      <TableCell align='center'>{row.title}</TableCell>
                      <TableCell align='center'>{row.institution}</TableCell>
                      <TableCell align='center'>{row.region?.parent?.name + ' ' + row.region?.name}</TableCell>
                      <TableCell align='center'>
                        {row.createdAt.slice(0, 10)}
                        <br></br>
                        {row.createdAt.slice(11)}
                      </TableCell>
                      <TableCell align='center'>
                        <Switch
                          onClick={(e) => e.stopPropagation()}
                          checked={row.status}
                          onChange={(e) => {
                            e.stopPropagation();
                            axiosInstance.put(`policy/${row.id}?sendNotification=false`, { status: e.target.checked }).then((res) => {
                              reloadTrigger();
                            });
                          }}
                          inputProps={{ 'aria-label': 'controlled' }}
                        />
                      </TableCell>
                    </TableRow>

                    <TableRow>
                      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={7}>
                        <Collapse in={row && userDetail && row.id === userDetail.id} timeout='auto' unmountOnExit>
                          <div style={{ width: '100%', padding: '20px 10px 30px 10px' }}>
                            {userDetail && (
                              <>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: 15 }}>
                                  <div style={{ display: 'flex', gap: 10 }}>
                                    <TextField
                                      label='title'
                                      variant='standard'
                                      sx={{ minWidth: '400px' }}
                                      size='small'
                                      value={userDetail.title}
                                      onChange={(e) => {
                                        let temp = { ...userDetail };
                                        temp.title = e.target.value;
                                        setUserDetail(temp);
                                      }}
                                    />
                                    <TextField
                                      label='institution'
                                      variant='standard'
                                      size='small'
                                      sx={{ flex: 1 }}
                                      value={userDetail.institution}
                                      onChange={(e) => {
                                        let temp = { ...userDetail };
                                        temp.institution = e.target.value;
                                        setUserDetail(temp);
                                      }}
                                    />
                                  </div>
                                  <TextField
                                    label='content'
                                    variant='standard'
                                    size='small'
                                    multiline
                                    fullWidth
                                    value={userDetail.content}
                                    onChange={(e) => {
                                      let temp = { ...userDetail };
                                      temp.content = e.target.value;
                                      setUserDetail(temp);
                                    }}
                                  />
                                  <div style={{ display: 'flex', gap: 10 }}>
                                    <TextField
                                      label='startDate'
                                      helperText='YYYY-MM-DD'
                                      error={!userDetail?.startDate?.match(/^\d{4}-\d{2}-\d{2}$/) && userDetail?.startDate}
                                      variant='standard'
                                      size='small'
                                      sx={{ flex: 1 }}
                                      value={userDetail.startDate}
                                      onChange={(e) => {
                                        let temp = { ...userDetail };
                                        temp.startDate = e.target.value;
                                        setUserDetail(temp);
                                      }}
                                    />
                                    <TextField
                                      label='endDate'
                                      variant='standard'
                                      helperText='YYYY-MM-DD'
                                      size='small'
                                      error={!userDetail?.endDate?.match(/^\d{4}-\d{2}-\d{2}$/) && userDetail?.endDate}
                                      sx={{ flex: 1 }}
                                      value={userDetail.endDate}
                                      onChange={(e) => {
                                        let temp = { ...userDetail };
                                        temp.endDate = e.target.value;
                                        setUserDetail(temp);
                                      }}
                                    />
                                  </div>
                                  <TextField
                                    label='period'
                                    variant='standard'
                                    size='small'
                                    sx={{ flex: 1 }}
                                    value={userDetail.period}
                                    onChange={(e) => {
                                      let temp = { ...userDetail };
                                      temp.period = e.target.value;
                                      setUserDetail(temp);
                                    }}
                                  />
                                  <TextField
                                    label='supportScale'
                                    variant='standard'
                                    size='small'
                                    value={userDetail.supportScale}
                                    onChange={(e) => {
                                      let temp = { ...userDetail };
                                      temp.supportScale = e.target.value;
                                      setUserDetail(temp);
                                    }}
                                  />
                                  <TextField
                                    label='detailUrl'
                                    variant='standard'
                                    size='small'
                                    multiline
                                    value={userDetail.detailUrl}
                                    onChange={(e) => {
                                      let temp = { ...userDetail };
                                      temp.detailUrl = e.target.value;
                                      setUserDetail(temp);
                                    }}
                                  />
                                  <TextField
                                    label='support'
                                    variant='standard'
                                    size='small'
                                    multiline
                                    sx={{ flex: 1 }}
                                    value={userDetail.support}
                                    onChange={(e) => {
                                      let temp = { ...userDetail };
                                      temp.support = e.target.value;
                                      setUserDetail(temp);
                                    }}
                                  />
                                  <TextField
                                    label='process'
                                    variant='standard'
                                    size='small'
                                    multiline
                                    sx={{ flex: 1 }}
                                    value={userDetail.process}
                                    onChange={(e) => {
                                      let temp = { ...userDetail };
                                      temp.process = e.target.value;
                                      setUserDetail(temp);
                                    }}
                                  />
                                  <TextField
                                    label='announcement'
                                    variant='standard'
                                    size='small'
                                    multiline
                                    sx={{ flex: 1 }}
                                    value={userDetail.announcement}
                                    onChange={(e) => {
                                      let temp = { ...userDetail };
                                      temp.announcement = e.target.value;
                                      setUserDetail(temp);
                                    }}
                                  />
                                  <div style={{ position: 'relative' }}>
                                    <div style={{ display: 'flex', gap: 20, float: 'left' }}>
                                      <FormControl sx={{ width: '90px' }} size='small'>
                                        <InputLabel id='demo-simple-select-label'>지역</InputLabel>
                                        <Select
                                          value={r1}
                                          onChange={(e) => {
                                            sr2();
                                            sr1(e.target.value);
                                          }}
                                        >
                                          {regions1.map((region, idx) => {
                                            return <MenuItem value={region.id}>{region.name}</MenuItem>;
                                          })}
                                        </Select>
                                      </FormControl>
                                      <FormControl sx={{ width: '90px' }} size='small'>
                                        <InputLabel id='demo-simple-select-label'>상세 지역</InputLabel>
                                        <Select value={r2} onChange={(e) => sr2(e.target.value)}>
                                          {regions2.map((region, idx) => {
                                            return <MenuItem value={region.id}>{region.name}</MenuItem>;
                                          })}
                                        </Select>
                                      </FormControl>

                                      <FormControl sx={{ width: '120px' }} size='small'>
                                        <InputLabel id='demo-simple-select-label'>상위카테</InputLabel>
                                        <Select
                                          value={c1}
                                          onChange={(e) => {
                                            sc2();
                                            sc1(e.target.value);
                                          }}
                                        >
                                          {cas1.map((region, idx) => {
                                            return <MenuItem value={region.id}>{region.name}</MenuItem>;
                                          })}
                                        </Select>
                                      </FormControl>
                                      <FormControl sx={{ width: '120px' }} size='small'>
                                        <InputLabel id='demo-simple-select-label'>하위카테</InputLabel>
                                        <Select
                                          value={c2}
                                          onChange={(e) => {
                                            sc2(e.target.value);
                                          }}
                                        >
                                          {cas2.map((region, idx) => {
                                            return <MenuItem value={region.id}>{region.name}</MenuItem>;
                                          })}
                                        </Select>
                                      </FormControl>
                                    </div>

                                    <Button
                                      variant='contained'
                                      sx={{ float: 'right', width: '70px' }}
                                      onClick={(e) => {
                                        updatePolicy(e, row, false);
                                      }}
                                    >
                                      수정
                                    </Button>
                                    <Button
                                      variant='contained'
                                      color='secondary'
                                      sx={{ float: 'right', width: '100px', marginRight: '20px' }}
                                      onClick={(e) => {
                                        if (!window.confirm('구독한 사용자에게 등록 알림을 보내며 수정합니다')) return;
                                        updatePolicy(e, row, true);
                                      }}
                                    >
                                      수정(알림)
                                    </Button>
                                    <Button
                                      size='small'
                                      variant='contained'
                                      color='error'
                                      sx={{ float: 'right', width: '30px', marginRight: '70px' }}
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        if (!window.confirm('정말 삭제하시겠습니까?')) return;

                                        axiosInstance
                                          .delete(`policy/${row.id}`)
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
                                  </div>
                                  <div style={{ display: 'flex', gap: 20 }}>
                                    <div>createdAt:{userDetail.createdAt}</div>
                                    <div>modifiedAt:{userDetail.modifiedAt}</div>
                                    <div>openApiType:{userDetail.openApiType}</div>
                                    <div>countOfInterest:{userDetail.countOfInterest}</div>
                                  </div>
                                </div>
                                {/* {Object.entries(userDetail).map(([key, value]) => (
                                  <div>
                                    {key}:{' ' + JSON.stringify(value)}
                                  </div>
                                ))} */}
                              </>
                            )}
                          </div>
                        </Collapse>
                      </TableCell>
                    </TableRow>
                  </>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <Pagination
            count={data?.totalPages}
            page={page}
            siblingCount={5}
            onChange={(e, value) => {
              setPage(value);
            }}
            color='primary'
          />

          <Modal open={open} onClose={handleClose} aria-labelledby='modal-modal-title' aria-describedby='modal-modal-description'>
            <Box sx={style}>
              <div style={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, marginTop: 30 }}>
                <div style={{ width: '100%', display: 'flex', gap: 20 }}>
                  <div style={{ width: '40%', display: 'flex', flexDirection: 'column', gap: 10 }}>
                    <FormControl>
                      <InputLabel id='demo-simple-select-label'>지역</InputLabel>
                      <Select size='small' value={region1} onChange={(e) => setRegion1(e.target.value)}>
                        {regions1.map((region, idx) => {
                          return <MenuItem value={region.id}>{region.name}</MenuItem>;
                        })}
                      </Select>
                    </FormControl>
                    <FormControl>
                      <InputLabel id='demo-simple-select-label'>상세 지역</InputLabel>
                      <Select size='small' value={region2} onChange={(e) => setPregion(e.target.value)}>
                        {regions2.map((region, idx) => {
                          return <MenuItem value={region.id}>{region.name}</MenuItem>;
                        })}
                      </Select>
                    </FormControl>
                  </div>

                  <div style={{ width: '40%', display: 'flex', flexDirection: 'column', gap: 10 }}>
                    <FormControl>
                      <InputLabel id='demo-simple-select-label'>상위카테</InputLabel>
                      <Select size='small' sx={{ width: 100 }} value={ca1} onChange={(e) => setCa1(e.target.value)}>
                        {cas1.map((region, idx) => {
                          return <MenuItem value={region.id}>{region.name}</MenuItem>;
                        })}
                      </Select>
                    </FormControl>
                    <FormControl>
                      <InputLabel id='demo-simple-select-label'>하위카테</InputLabel>
                      <Select
                        size='small'
                        sx={{ width: 100 }}
                        value={ca2}
                        onChange={(e) => {
                          setPcategory(e.target.value);
                        }}
                      >
                        {cas2.map((region, idx) => {
                          return <MenuItem value={region}>{region.name}</MenuItem>;
                        })}
                      </Select>
                    </FormControl>
                  </div>
                </div>

                <TextField
                  label='제목'
                  value={title}
                  onChange={(e) => {
                    setTitle(e.target.value);
                  }}
                ></TextField>

                <TextField
                  label='기관'
                  value={institution}
                  onChange={(e) => {
                    setInstitution(e.target.value);
                  }}
                ></TextField>

                <TextField
                  label='내용'
                  value={content}
                  onChange={(e) => {
                    setContent(e.target.value);
                  }}
                ></TextField>
                <div style={{ width: '100%', display: 'flex', justifyContent: 'center', gap: 20 }}>
                  <Button
                    variant='contained'
                    sx={{ marginBottom: 0.5 }}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleClose();
                      axiosInstance
                        .post(`policy`, [{ title, institution, content, region: { id: pregion }, category: { id: pcategory.id } }])
                        .then((res) => {
                          fetchData();
                        })
                        .catch((res) => {
                          alert(res.response.data.message);
                        });
                    }}
                  >
                    정책 추가
                  </Button>
                </div>
              </div>
            </Box>
          </Modal>
        </>
      )}
    </div>
  );
}

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
