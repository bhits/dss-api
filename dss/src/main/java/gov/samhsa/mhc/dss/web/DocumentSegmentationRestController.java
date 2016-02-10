package gov.samhsa.mhc.dss.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Jiahao.Li on 2/10/2016.
 */
@RestController
public class DocumentSegmentationRestController {

    @RequestMapping("/")
    public String index() {
        return "Greetings from Document Segmentation Service API!";
    }
}
