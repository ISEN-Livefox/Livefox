package com.livefox.clientui.proxies;


import com.livefox.clientui.bean.VideoBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "mvideo-client", url = "mvideo-client:9001")
public interface VideoProxy {

    @GetMapping(value = "/video")
    List<VideoBean> listVideo();

    @PostMapping(value = "/video/add")
    public void addVideo (@Valid @RequestBody VideoBean video);

}
