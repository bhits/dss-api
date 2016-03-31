package gov.samhsa.mhc.dss.infrastructure.valueset;

import gov.samhsa.mhc.common.filereader.FileReader;
import gov.samhsa.mhc.dss.infrastructure.valueset.dto.ConceptCodeAndCodeSystemOidDto;
import gov.samhsa.mhc.dss.infrastructure.valueset.dto.ValueSetQueryDto;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class ValueSetServiceImplMock implements ValueSetService {
    private static final String VALUE_SET_MOCK_DATA_PATH = "MockValueSetData.csv";
    private FileReader fileReader;
    private List<ConceptCode> conceptCodeList;

    public ValueSetServiceImplMock() {
    }

    public ValueSetServiceImplMock(FileReader fileReader) {
        this.fileReader = fileReader;
        try {
            init();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return conceptCodeList.toString();
    }

    private void init() throws IOException {
        conceptCodeList = new LinkedList<ConceptCode>();
        // FileInputStream fis = new FileInputStream(VALUE_SET_MOCK_DATA_PATH);
        // File file = new File(fis);
        String file = fileReader.readFile(VALUE_SET_MOCK_DATA_PATH);
        // System.out.println(file);
        Scanner scanFile = new Scanner(file);
        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            Scanner scanLine = new Scanner(line);
            scanLine.useDelimiter(",");
            ConceptCode code = new ConceptCode();
            while (scanLine.hasNext()) {
                String next = scanLine.next();
                if ("304.31".equals(next)) {
                    // System.out.println(line);
                }
                code.setVariable(next);
            }
            conceptCodeList.add(code);
        }
        // System.out.println("Initialized Value Set Servise with size:"+conceptCodeList.size());
    }

    private boolean isEqual(ConceptCode c1, String code, String codeSystem) {
        return c1.getCode().equals(code)
                && c1.getCodeSystem().equals(codeSystem);
    }

    @Override
    public List<ValueSetQueryDto> lookupValueSetCategories(@Valid @RequestBody List<ConceptCodeAndCodeSystemOidDto> conceptCodeAndCodeSystemOidDtos) {
        return conceptCodeAndCodeSystemOidDtos.stream()
                .map(dto -> {
                    final Optional<ConceptCode> any = conceptCodeList.stream()
                            .filter(cc -> dto.getConceptCode().equals(cc.getCode()) && dto.getCodeSystemOid().equals(cc.getCodeSystem()))
                            .findAny();
                    return any.map(cc -> {
                        ValueSetQueryDto r = new ValueSetQueryDto();
                        r.setConceptCode(dto.getConceptCode());
                        r.setCodeSystemOid(dto.getCodeSystemOid());
                        Set<String> vs = new HashSet<>();
                        vs.add(cc.getValueSetCategory());
                        r.setVsCategoryCodes(vs);
                        return r;
                    }).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }


}
