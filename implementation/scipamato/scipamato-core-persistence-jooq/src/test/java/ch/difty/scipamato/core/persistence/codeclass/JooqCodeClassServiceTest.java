package ch.difty.scipamato.core.persistence.codeclass;

import static ch.difty.scipamato.core.entity.CodeClass.CodeClassFields.NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.extractProperty;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.difty.scipamato.core.entity.CodeClass;

@RunWith(MockitoJUnitRunner.class)
public class JooqCodeClassServiceTest {

    @Mock
    private CodeClassRepository repoMock;

    @Test
    public void findingCodeClass_delegatesToRepo() {
        JooqCodeClassService service = new JooqCodeClassService(repoMock);

        String languageCodeClass = "de";

        List<CodeClass> ccs = new ArrayList<>();
        ccs.add(new CodeClass(1, "cc1", ""));
        ccs.add(new CodeClass(2, "cc2", ""));
        when(repoMock.find(languageCodeClass)).thenReturn(ccs);

        assertThat(extractProperty(NAME.getName()).from(service.find(languageCodeClass))).containsOnly("cc1", "cc2");

        verify(repoMock).find(languageCodeClass);

        verifyNoMoreInteractions(repoMock);
    }
}
