package com.classroomapp.classroombackend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VietnameseEncodingFixService implements CommandLineRunner {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    // Mapping cÃ¡Â»Â§a cÃƒÂ¡c kÃƒÂ½ tÃ¡Â»Â± bÃ¡Â»â€¹ lÃ¡Â»â€”i encoding thÃ†Â°Ã¡Â»Âng gÃ¡ÂºÂ·p
    private static final Map<String, String> ENCODING_FIX_MAP = new HashMap<>();
    
    static {
        // CÃƒÂ¡c kÃƒÂ½ tÃ¡Â»Â± tiÃ¡ÂºÂ¿ng ViÃ¡Â»â€¡t thÃ†Â°Ã¡Â»Âng bÃ¡Â»â€¹ lÃ¡Â»â€”i
        // Add specific error message mappings
        ENCODING_FIX_MAP.put("KhÃ´ng tÃ¬m tháº¥y", "Không tìm thấy");
        ENCODING_FIX_MAP.put("YÃªu cáº§u khÃ´ng há»£p lá»‡", "Yêu cầu không hợp lệ");
        ENCODING_FIX_MAP.put("TÃ i nguyÃªn yÃªu cáº§u khÃ´ng tá»n táº¡i", "Tài nguyên yêu cầu không tồn tại");
        
        ENCODING_FIX_MAP.put("ÃƒÆ'Ã‚Â¡", "ÃƒÂ¡");
        ENCODING_FIX_MAP.put("ÃƒÆ’ ", "ÃƒÂ ");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â£", "Ã¡ÂºÂ£");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â£", "ÃƒÂ£");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â¡", "Ã¡ÂºÂ¡");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â¢", "ÃƒÂ¢");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â¥", "Ã¡ÂºÂ¥");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â§", "Ã¡ÂºÂ§");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â©", "Ã¡ÂºÂ©");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â«", "Ã¡ÂºÂ«");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â­", "Ã¡ÂºÂ­");
        ENCODING_FIX_MAP.put("Ãƒâ€ž", "Ã„Æ’");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â¯", "Ã¡ÂºÂ¯");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â±", "Ã¡ÂºÂ±");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â³", "Ã¡ÂºÂ³");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â¡", "Ã¡ÂºÂ¡");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â©", "ÃƒÂ©");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â¨", "ÃƒÂ¨");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â»", "Ã¡ÂºÂ»");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â½", "Ã¡ÂºÂ½");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â¹", "Ã¡ÂºÂ¹");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Âª", "ÃƒÂª");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚ÂºÃ‚Â¿", "Ã¡ÂºÂ¿");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Â");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Æ’");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â€¦");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â€¡");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â­", "ÃƒÂ­");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â¬", "ÃƒÂ¬");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â€°");
        ENCODING_FIX_MAP.put("Ãƒâ€žÃ‚Â©", "Ã„Â©");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â€¹");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â³", "ÃƒÂ³");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â²", "ÃƒÂ²");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Â");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Âµ", "ÃƒÂµ");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Â");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â´", "ÃƒÂ´");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â€˜");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â€œ");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â€¢");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â€”");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â„¢");
        ENCODING_FIX_MAP.put("Ãƒâ€ Ã‚Â¡", "Ã†Â¡");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»â€º");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Â");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Å¸");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Â¡");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»Ã‚Â£", "Ã¡Â»Â£");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Âº", "ÃƒÂº");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â¹", "ÃƒÂ¹");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»Ã‚Â§", "Ã¡Â»Â§");
        ENCODING_FIX_MAP.put("Ãƒâ€¦Ã‚Â©", "Ã…Â©");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»Ã‚Â¥", "Ã¡Â»Â¥");
        ENCODING_FIX_MAP.put("Ãƒâ€ Ã‚Â°", "Ã†Â°");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Â©");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Â«");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Â­");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»", "Ã¡Â»Â¯");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»Ã‚Â±", "Ã¡Â»Â±");
        ENCODING_FIX_MAP.put("ÃƒÆ’Ã‚Â½", "ÃƒÂ½");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»Ã‚Â³", "Ã¡Â»Â³");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»Ã‚Â·", "Ã¡Â»Â·");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»Ã‚Â¹", "Ã¡Â»Â¹");
        ENCODING_FIX_MAP.put("ÃƒÂ¡Ã‚Â»Ã‚Âµ", "Ã¡Â»Âµ");
        ENCODING_FIX_MAP.put("Ãƒâ€ž", "Ã„â€˜");
        
        // CÃƒÂ¡c pattern phÃ¡Â»â€¢ biÃ¡ÂºÂ¿n bÃ¡Â»â€¹ lÃ¡Â»â€”i
        ENCODING_FIX_MAP.put("c?p", "cÃ¡ÂºÂ¥p");
        ENCODING_FIX_MAP.put("h?c", "hÃ¡Â»Âc");
        ENCODING_FIX_MAP.put("Vi?t", "ViÃ¡Â»â€¡t");
        ENCODING_FIX_MAP.put("ti?ng", "tiÃ¡ÂºÂ¿ng");
        ENCODING_FIX_MAP.put("Ti?ng", "TiÃ¡ÂºÂ¿ng");
        ENCODING_FIX_MAP.put("ngh?", "nghÃ¡Â»â€¡");
        ENCODING_FIX_MAP.put("co b?n", "cÃ†Â¡ bÃ¡ÂºÂ£n");
        ENCODING_FIX_MAP.put("l?p", "lÃ¡ÂºÂ­p");
        ENCODING_FIX_MAP.put("L?p", "LÃ¡ÂºÂ­p");
        ENCODING_FIX_MAP.put("Nguy?n", "NguyÃ¡Â»â€¦n");
        ENCODING_FIX_MAP.put("Tr?n", "TrÃ¡ÂºÂ§n");
        ENCODING_FIX_MAP.put("Th?", "ThÃ¡Â»â€¹");
        ENCODING_FIX_MAP.put("Ph?m", "PhÃ¡ÂºÂ¡m");
        ENCODING_FIX_MAP.put("Van", "VÃ„Æ’n");
        ENCODING_FIX_MAP.put("t?p", "tÃ¡ÂºÂ­p");
        ENCODING_FIX_MAP.put("BÃƒÂ i t?p", "BÃƒÂ i tÃ¡ÂºÂ­p");
        ENCODING_FIX_MAP.put("v?", "vÃ¡Â»Â");
        ENCODING_FIX_MAP.put("Ma tr?n", "Ma trÃ¡ÂºÂ­n");
        ENCODING_FIX_MAP.put("D?nh", "Ã„ÂÃ¡Â»â€¹nh");
        ENCODING_FIX_MAP.put("th?c", "thÃ¡Â»Â©c");
        ENCODING_FIX_MAP.put("tÃƒÂ¡c ph?m", "tÃƒÂ¡c phÃ¡ÂºÂ©m");
        ENCODING_FIX_MAP.put("tho", "thÃ†Â¡");
        ENCODING_FIX_MAP.put("H?", "HÃ¡Â»â€œ");
        ENCODING_FIX_MAP.put("Ki?m", "KiÃ¡Â»Æ’m");
        ENCODING_FIX_MAP.put("gi?a", "giÃ¡Â»Â¯a");
        ENCODING_FIX_MAP.put("k?", "kÃ¡Â»Â³");
        ENCODING_FIX_MAP.put("cu?i", "cuÃ¡Â»â€˜i");
        ENCODING_FIX_MAP.put("h?t", "hÃ¡ÂºÂ¿t");
        ENCODING_FIX_MAP.put("mÃƒÂ´n", "mÃƒÂ´n");
        ENCODING_FIX_MAP.put("V?n", "VÃ„Æ’n");
        ENCODING_FIX_MAP.put("th?c", "thÃ¡Â»Â±c");
        ENCODING_FIX_MAP.put("hÃƒÂ nh", "hÃƒÂ nh");
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Ã°Å¸â€Â§ BÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u kiÃ¡Â»Æ’m tra vÃƒÂ  sÃ¡Â»Â­a lÃ¡Â»â€”i encoding tiÃ¡ÂºÂ¿ng ViÃ¡Â»â€¡t...");
        
        try {
            int fixedCount = 0;
            
            // Fix classroom names
            fixedCount += fixClassroomNames();
            
            // Fix user names
            fixedCount += fixUserNames();
            
            // Fix assignment titles and descriptions
            fixedCount += fixAssignmentData();
            
            // Fix submission comments and feedback
            fixedCount += fixSubmissionData();
            
            if (fixedCount > 0) {
                log.info("Ã¢Å“â€¦ Ã„ÂÃƒÂ£ sÃ¡Â»Â­a {} lÃ¡Â»â€”i encoding tiÃ¡ÂºÂ¿ng ViÃ¡Â»â€¡t", fixedCount);
            } else {
                log.info("Ã¢Å“â€¦ KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y lÃ¡Â»â€”i encoding nÃƒÂ o cÃ¡ÂºÂ§n sÃ¡Â»Â­a");
            }
            
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ LÃ¡Â»â€”i khi sÃ¡Â»Â­a encoding tiÃ¡ÂºÂ¿ng ViÃ¡Â»â€¡t: {}", e.getMessage(), e);
        }
    }

    private int fixClassroomNames() {
        int fixedCount = 0;
        var classrooms = classroomRepository.findAll();
        
        for (var classroom : classrooms) {
            if (classroom.getName() != null) {
                String originalName = classroom.getName();
                String fixedName = fixVietnameseText(originalName);
                
                if (!originalName.equals(fixedName)) {
                    classroom.setName(fixedName);
                    classroomRepository.save(classroom);
                    fixedCount++;
                    log.info("Ã°Å¸â€œÂ SÃ¡Â»Â­a tÃƒÂªn lÃ¡Â»â€ºp: '{}' -> '{}'", originalName, fixedName);
                }
            }
            
            if (classroom.getDescription() != null) {
                String originalDesc = classroom.getDescription();
                String fixedDesc = fixVietnameseText(originalDesc);
                
                if (!originalDesc.equals(fixedDesc)) {
                    classroom.setDescription(fixedDesc);
                    classroomRepository.save(classroom);
                    fixedCount++;
                    log.info("Ã°Å¸â€œÂ SÃ¡Â»Â­a mÃƒÂ´ tÃ¡ÂºÂ£ lÃ¡Â»â€ºp: '{}' -> '{}'", originalDesc, fixedDesc);
                }
            }
        }
        
        return fixedCount;
    }

    private int fixUserNames() {
        int fixedCount = 0;
        var users = userRepository.findAll();
        
        for (var user : users) {
            if (user.getFullName() != null) {
                String originalName = user.getFullName();
                String fixedName = fixVietnameseText(originalName);
                
                if (!originalName.equals(fixedName)) {
                    user.setFullName(fixedName);
                    userRepository.save(user);
                    fixedCount++;
                    log.info("Ã°Å¸â€˜Â¤ SÃ¡Â»Â­a tÃƒÂªn ngÃ†Â°Ã¡Â»Âi dÃƒÂ¹ng: '{}' -> '{}'", originalName, fixedName);
                }
            }
        }
        
        return fixedCount;
    }

    private int fixAssignmentData() {
        int fixedCount = 0;
        var assignments = assignmentRepository.findAll();
        
        for (var assignment : assignments) {
            if (assignment.getTitle() != null) {
                String originalTitle = assignment.getTitle();
                String fixedTitle = fixVietnameseText(originalTitle);
                
                if (!originalTitle.equals(fixedTitle)) {
                    assignment.setTitle(fixedTitle);
                    assignmentRepository.save(assignment);
                    fixedCount++;
                    log.info("Ã°Å¸â€œÅ¡ SÃ¡Â»Â­a tiÃƒÂªu Ã„â€˜Ã¡Â»Â bÃƒÂ i tÃ¡ÂºÂ­p: '{}' -> '{}'", originalTitle, fixedTitle);
                }
            }
            
            if (assignment.getDescription() != null) {
                String originalDesc = assignment.getDescription();
                String fixedDesc = fixVietnameseText(originalDesc);
                
                if (!originalDesc.equals(fixedDesc)) {
                    assignment.setDescription(fixedDesc);
                    assignmentRepository.save(assignment);
                    fixedCount++;
                    log.info("Ã°Å¸â€œÅ¡ SÃ¡Â»Â­a mÃƒÂ´ tÃ¡ÂºÂ£ bÃƒÂ i tÃ¡ÂºÂ­p: '{}' -> '{}'", originalDesc, fixedDesc);
                }
            }
        }
        
        return fixedCount;
    }

    private int fixSubmissionData() {
        int fixedCount = 0;
        var submissions = submissionRepository.findAll();
        
        for (var submission : submissions) {
            if (submission.getComment() != null) {
                String originalComment = submission.getComment();
                String fixedComment = fixVietnameseText(originalComment);
                
                if (!originalComment.equals(fixedComment)) {
                    submission.setComment(fixedComment);
                    submissionRepository.save(submission);
                    fixedCount++;
                    log.info("Ã°Å¸â€™Â¬ SÃ¡Â»Â­a comment bÃƒÂ i nÃ¡Â»â„¢p: '{}' -> '{}'", originalComment, fixedComment);
                }
            }
            
            if (submission.getFeedback() != null) {
                String originalFeedback = submission.getFeedback();
                String fixedFeedback = fixVietnameseText(originalFeedback);
                
                if (!originalFeedback.equals(fixedFeedback)) {
                    submission.setFeedback(fixedFeedback);
                    submissionRepository.save(submission);
                    fixedCount++;
                    log.info("Ã°Å¸â€œÂ SÃ¡Â»Â­a feedback bÃƒÂ i nÃ¡Â»â„¢p: '{}' -> '{}'", originalFeedback, fixedFeedback);
                }
            }
        }
        
        return fixedCount;
    }

    /**
     * SÃ¡Â»Â­a text tiÃ¡ÂºÂ¿ng ViÃ¡Â»â€¡t bÃ¡Â»â€¹ lÃ¡Â»â€”i encoding
     * 
     * @param text Text cÃ¡ÂºÂ§n sÃ¡Â»Â­a
     * @return Text Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c sÃ¡Â»Â­a
     */
    public String fixVietnameseText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String fixedText = text;
        
        // ÃƒÂp dÃ¡Â»Â¥ng cÃƒÂ¡c fix tÃ¡Â»Â« mapping
        for (Map.Entry<String, String> entry : ENCODING_FIX_MAP.entrySet()) {
            fixedText = fixedText.replace(entry.getKey(), entry.getValue());
        }
        
        // SÃ¡Â»Â­ dÃ¡Â»Â¥ng regex Ã„â€˜Ã¡Â»Æ’ fix cÃƒÂ¡c pattern phÃ¡Â»â€¢ biÃ¡ÂºÂ¿n
        // Fix dÃ¡ÂºÂ¥u hÃ¡Â»Âi chÃ¡ÂºÂ¥m trong tiÃ¡ÂºÂ¿ng ViÃ¡Â»â€¡t
        fixedText = fixedText.replaceAll("\\b([A-Za-z]+)\\?([a-z]+)\\b", "$1Ã¡Â»Â$2");
        fixedText = fixedText.replaceAll("\\b([A-Za-z]+)\\?([A-Za-z]+)\\b", "$1Ã¡Â»â€¡$2");
        
        return fixedText;
    }

    /**
     * KiÃ¡Â»Æ’m tra xem text cÃƒÂ³ chÃ¡Â»Â©a kÃƒÂ½ tÃ¡Â»Â± tiÃ¡ÂºÂ¿ng ViÃ¡Â»â€¡t bÃ¡Â»â€¹ lÃ¡Â»â€”i encoding khÃƒÂ´ng
     * 
     * @param text Text cÃ¡ÂºÂ§n kiÃ¡Â»Æ’m tra
     * @return true nÃ¡ÂºÂ¿u cÃƒÂ³ lÃ¡Â»â€”i encoding
     */
    public boolean hasEncodingIssues(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // KiÃ¡Â»Æ’m tra cÃƒÂ¡c pattern thÃ†Â°Ã¡Â»Âng gÃ¡ÂºÂ·p
        return text.contains("?") && (
            text.contains("c?p") || 
            text.contains("h?c") || 
            text.contains("Vi?t") || 
            text.contains("ngh?") || 
            text.contains("t?p") ||
            text.contains("Nguy?n") ||
            text.contains("Tr?n") ||
            text.contains("Ph?m")
        );
    }

    /**
     * Validate Vietnamese text encoding
     * 
     * @param text Text to validate
     * @return Validation result
     */
    public ValidationResult validateVietnameseEncoding(String text) {
        if (text == null || text.isEmpty()) {
            return ValidationResult.builder()
                .isValid(true)
                .message("Text is null or empty")
                .build();
        }
        
        boolean hasIssues = hasEncodingIssues(text);
        String fixedText = hasIssues ? fixVietnameseText(text) : text;
        
        return ValidationResult.builder()
            .isValid(!hasIssues)
            .originalText(text)
            .fixedText(fixedText)
            .message(hasIssues ? "Text has encoding issues" : "Text encoding is valid")
            .build();
    }

    /**
     * KÃ¡ÂºÂ¿t quÃ¡ÂºÂ£ validation
     */
    public static class ValidationResult {
        private boolean isValid;
        private String originalText;
        private String fixedText;
        private String message;
        
        public static ValidationResultBuilder builder() {
            return new ValidationResultBuilder();
        }
        
        // Getters
        public boolean isValid() { return isValid; }
        public String getOriginalText() { return originalText; }
        public String getFixedText() { return fixedText; }
        public String getMessage() { return message; }
        
        // Builder pattern
        public static class ValidationResultBuilder {
            private boolean isValid;
            private String originalText;
            private String fixedText;
            private String message;
            
            public ValidationResultBuilder isValid(boolean isValid) {
                this.isValid = isValid;
                return this;
            }
            
            public ValidationResultBuilder originalText(String originalText) {
                this.originalText = originalText;
                return this;
            }
            
            public ValidationResultBuilder fixedText(String fixedText) {
                this.fixedText = fixedText;
                return this;
            }
            
            public ValidationResultBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public ValidationResult build() {
                ValidationResult result = new ValidationResult();
                result.isValid = this.isValid;
                result.originalText = this.originalText;
                result.fixedText = this.fixedText;
                result.message = this.message;
                return result;
            }
        }
    }
} 
