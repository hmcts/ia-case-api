package uk.gov.hmcts.reform.iacaseapi;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacaseapi.fixtures.Fixture;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdDataApi;

@SpringBootTest
@ActiveProfiles("functional")
public class CaseAccessFunctionalTest extends CcdCaseCreationTest {
    @Autowired
    protected CcdDataApi ccdApi;

    @Autowired
    protected List<Fixture> fixtures;
}
