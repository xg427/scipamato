package ch.difty.scipamato.core.entity.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import ch.difty.scipamato.core.entity.Code;

public class SearchConditionCodeBoxTest {

    private final SearchConditionCodeBox cb = new SearchConditionCodeBox();

    @Test
    public void emptyCodeBox_resultsInEmptyString() {
        assertThat(cb.toString()).isEmpty();
    }

    @Test
    public void codeBoxWithOneMember_resultsInCode() {
        cb.addCode(new Code("1F", "c1f", "comm", false, 1, "CC1", "", 0));
        assertThat(cb.toString()).isEqualTo("1F");
    }

    @Test
    public void codeBoxWithMultipleMembers_resultsInConcatenatedCodes() {
        cb.addCode(new Code("1F", "c1f", "comm1", false, 1, "CC1", "", 0));
        cb.addCode(new Code("1G", "c1g", "comm2", true, 1, "CC1", "", 2));
        cb.addCode(new Code("2A", "c2a", "comm3", false, 2, "CC2", "", 1));
        assertThat(cb.toString()).isEqualTo("1F&1G&2A");
    }
}
