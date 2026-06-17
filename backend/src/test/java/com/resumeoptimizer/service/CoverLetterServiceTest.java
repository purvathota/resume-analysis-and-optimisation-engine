package com.resumeoptimizer.service.coverletter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.resumeoptimizer.repository.CoverLetterRepository;
import com.resumeoptimizer.repository.JobDescriptionRepository;
import com.resumeoptimizer.repository.ResumeRepository;
import com.resumeoptimizer.service.ai.AiClientService;
import com.resumeoptimizer.service.ai.CoverLetterPromptService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

public class CoverLetterServiceTest {

    @Mock
    private CoverLetterRepository coverLetterRepository;
    @Mock
    private com.resumeoptimizer.repository.CoverLetterVersionRepository coverLetterVersionRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private JobDescriptionRepository jobDescriptionRepository;
    @Mock
    private AiClientService aiClientService;
    @Mock
    private CoverLetterPromptService promptService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CoverLetterService coverLetterService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHasUncertaintyPhrases_DetectsForbiddenPhrases() {
        // Forbidden phrases
        assertTrue(coverLetterService.hasUncertaintyPhrases("While my resume does not show..."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("Although my resume doesn't have details..."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("My resume may not explicitly show my experience withWise..."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("Despite the lack of direct cloud experience..."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("While I do not have direct experience, I am eager to learn."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("I note my lack of experience in Java."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("I may not have the exact background..."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("I haven’t had the opportunity to use AWS."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("I haven't had the opportunity to work in finance."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("My background does not directly match..."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("This is not explicitly shown in my history."));
        assertTrue(coverLetterService.hasUncertaintyPhrases("Despite not having prior experience..."));
    }

    @Test
    public void testHasUncertaintyPhrases_AllowsCleanText() {
        // Confident, professional phrases
        assertFalse(coverLetterService.hasUncertaintyPhrases("I am a highly motivated engineer with strong problem-solving skills."));
        assertFalse(coverLetterService.hasUncertaintyPhrases(null));
        assertFalse(coverLetterService.hasUncertaintyPhrases(""));
    }
}
