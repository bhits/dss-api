package gov.samhsa.c2s.dss.infrastructure.valueset;

import gov.samhsa.c2s.common.filereader.FileReader;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ConceptCodeAndCodeSystemOidDto;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ValueSetQueryDto;
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
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return conceptCodeList.toString();
    }

    private void init() throws IOException {
        conceptCodeList = new LinkedList<>();
        String file = fileReader.readFile(VALUE_SET_MOCK_DATA_PATH);
        Scanner scanFile = new Scanner(file);
        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            Scanner scanLine = new Scanner(line);
            scanLine.useDelimiter(",");
            ConceptCode code = new ConceptCode();
            while (scanLine.hasNext()) {
                String next = scanLine.next();
                if ("304.31".equals(next)) {
                }
                code.setVariable(next);
            }
            conceptCodeList.add(code);
        }
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
