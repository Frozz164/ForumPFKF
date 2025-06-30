package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CharityRequest;
import org.example.dto.CharityResponse;
import org.example.dto.DocumentDTO;
import org.example.dto.FundraisingResponse;
import org.example.model.Charity;
import org.example.model.Document;
import org.example.model.Fundraising;
import org.example.model.User;
import org.example.model.Donation;
import org.example.repository.CharityRepository;
import org.example.repository.DonationRepository;
import org.example.repository.FundraisingRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharityService {

    private final CharityRepository charityRepository;
    private final FundraisingRepository fundraisingRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;

    @Transactional
    public CharityResponse createCharity(CharityRequest request, Long userId) {
        log.info("Создание новой благотворительной организации: {}", request.getName());
        log.info("Категории: {}", request.getCategories());

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new RuntimeException("Пользователь не найден");
                });

        // Проверяем, не существует ли уже организация с таким регистрационным номером
        if (charityRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            log.error("Организация с таким регистрационным номером уже существует: {}", request.getRegistrationNumber());
            throw new RuntimeException("Организация с таким регистрационным номером уже существует");
        }

        if (request.getOrganizationName() == null || request.getOrganizationName().trim().isEmpty() ||
            request.getInn() == null || request.getInn().trim().isEmpty() ||
            request.getKpp() == null || request.getKpp().trim().isEmpty() ||
            request.getAccountNumber() == null || request.getAccountNumber().trim().isEmpty() ||
            request.getBik() == null || request.getBik().trim().isEmpty() ||
            request.getBankName() == null || request.getBankName().trim().isEmpty()) {
            log.error("Не указаны все обязательные банковские реквизиты");
            throw new RuntimeException("Все банковские реквизиты обязательны для заполнения");
        }

        // Создаем благотворительную организацию
        Charity charity = new Charity();
        charity.setName(request.getName());
        charity.setDescription(request.getDescription());
        charity.setWebsiteUrl(request.getWebsiteUrl());
        charity.setCategories(request.getCategories());
        charity.setRegistrationNumber(request.getRegistrationNumber());
        charity.setContactEmail(request.getContactEmail());
        charity.setContactPhone(request.getContactPhone());
        charity.setContactAddress(request.getContactAddress());
        charity.setVerified(false);
        charity.setCreatedAt(LocalDateTime.now());
        charity.setCreatedBy(creator);

        // Устанавливаем банковские реквизиты
        charity.setOrganizationName(request.getOrganizationName().trim());
        charity.setInn(request.getInn().trim());
        charity.setKpp(request.getKpp().trim());
        charity.setAccountNumber(request.getAccountNumber().trim());
        charity.setBik(request.getBik().trim());
        charity.setBankName(request.getBankName().trim());

        // Сохраняем организацию
        //charity = charityRepository.save(charity);
        log.info("Благотворительная организация успешно создана: {}", charity.getId());

        // Создаем основной фандрайзинг для общего фонда помощи
        Fundraising generalFund = new Fundraising();
        generalFund.setCharity(charity);
        generalFund.setCreatedBy(creator);
        generalFund.setTitle("Общий фонд помощи");
        generalFund.setDescription("Основной фонд для сбора пожертвований");
        generalFund.setTargetAmount(new BigDecimal("999999999999"));
        generalFund.setCurrentAmount(BigDecimal.ZERO);
        generalFund.setStartDate(LocalDateTime.now());
        generalFund.setActive(true);
        
        // Если есть документы, добавляем их в основной фандрайзинг
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            List<Document> documents = new ArrayList<>();
            List<String> titles = request.getDocumentTitles();
            List<String> descriptions = request.getDocumentDescriptions();
            
            for (int i = 0; i < request.getDocuments().size(); i++) {
                MultipartFile file = request.getDocuments().get(i);
                String title = (titles != null && titles.size() > i) ? titles.get(i) : "";
                String description = (descriptions != null && descriptions.size() > i) ? descriptions.get(i) : "";
                
                try {
                    // Генерируем уникальное имя файла
                    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path uploadPath = Paths.get("uploads");
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    // Сохраняем файл
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // Создаем документ
                    Document document = new Document();
                    document.setUrl("/uploads/" + fileName);
                    document.setTitle(title);
                    document.setDescription(description);
                    documents.add(document);
                    
                    log.info("Документ успешно сохранен: {}", document);
                } catch (IOException e) {
                    log.error("Ошибка при сохранении документа", e);
                    throw new RuntimeException("Не удалось сохранить документ", e);
                }
            }
            
            generalFund.setDocuments(documents);
            charity.setDocuments(documents);
        }

        // Сохраняем основной фандрайзинг
        charity = charityRepository.save(charity);
        generalFund = fundraisingRepository.save(generalFund);
        log.info("Основной фандрайзинг создан: {}", generalFund.getId());

        return convertToResponse(charity);
    }

    @Transactional(readOnly = true)
    public List<CharityResponse> getAllCharities() {
        log.debug("Получение списка всех благотворительных организаций");
        return charityRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CharityResponse getCharityById(Long id) {
        log.debug("Поиск благотворительной организации по ID: {}", id);
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Благотворительная организация с ID {} не найдена", id);
                    return new RuntimeException("Благотворительная организация не найдена");
                });
        return convertToResponse(charity);
    }

    @Transactional
    public CharityResponse updateCharity(Long id, CharityRequest request, Long userId) {
        log.info("Обновление данных благотворительной организации с ID: {}", id);
        
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Невозможно обновить: организация с ID {} не найдена", id);
                    return new RuntimeException("Благотворительная организация не найдена");
                });

        // Проверяем, является ли пользователь создателем фонда
        if (!charity.getCreatedBy().getId().equals(userId)) {
            log.error("Пользователь {} не является создателем фонда {}", userId, id);
            throw new RuntimeException("Только создатель фонда может обновлять его данные");
        }

        if (!charity.getRegistrationNumber().equals(request.getRegistrationNumber()) &&
            charityRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            log.warn("Попытка обновления на существующий регистрационный номер: {}", 
                    request.getRegistrationNumber());
            throw new RuntimeException("Организация с таким регистрационным номером уже существует");
        }

        charity.setName(request.getName());
        charity.setDescription(request.getDescription());
        charity.setWebsiteUrl(request.getWebsiteUrl());
        charity.setCategories(request.getCategories());
        charity.setRegistrationNumber(request.getRegistrationNumber());
        charity.setContactEmail(request.getContactEmail());
        charity.setContactPhone(request.getContactPhone());
        charity.setContactAddress(request.getContactAddress());

        charity = charityRepository.save(charity);
        log.info("Данные благотворительной организации успешно обновлены: {}", charity.getId());
        
        return convertToResponse(charity);
    }

    @Transactional(readOnly = true)
    public List<CharityResponse> getCharitiesByCategory(String category) {
        log.debug("Поиск благотворительных организаций по категории: {}", category);
        return charityRepository.findByCategory(category).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCharity(Long id) {
        log.info("Запрос на удаление благотворительной организации с ID: {}", id);
        
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Невозможно удалить: организация с ID {} не найдена", id);
                    return new RuntimeException("Благотворительная организация не найдена");
                });

        // Получаем список активных фандрайзинговых кампаний
        List<Fundraising> activeFundraisings = fundraisingRepository.findByCharityId(id).stream()
                .filter(f -> f.isActive())
                .collect(Collectors.toList());

        // Проверяем количество активных кампаний
        // Если есть только одна кампания (общий сбор) или нет активных кампаний, разрешаем удаление
        if (activeFundraisings.size() > 1) {
            log.error("Невозможно удалить организацию {}: есть активные кампании кроме общего сбора", id);
            throw new RuntimeException("Невозможно удалить организацию, пока есть активные фандрайзинговые кампании кроме общего сбора");
        }

        // Удаляем все фандрайзинговые кампании
        fundraisingRepository.deleteAll(fundraisingRepository.findByCharityId(id));
        
        // Удаляем сам фонд
        charityRepository.delete(charity);
        log.info("Благотворительная организация {} успешно удалена", id);
    }

    @Transactional
    public CharityResponse verifyCharity(Long id) {
        log.info("Верификация благотворительной организации с ID: {}", id);
        
        Charity charity = charityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Невозможно верифицировать: организация с ID {} не найдена", id);
                    return new RuntimeException("Благотворительная организация не найдена");
                });

        charity.setVerified(true);
        charity = charityRepository.save(charity);
        log.info("Благотворительная организация {} успешно верифицирована", id);
        
        return convertToResponse(charity);
    }

    @Transactional
    public CharityResponse uploadDocuments(Long charityId, List<MultipartFile> documents, 
            List<String> titles, List<String> descriptions, Long userId) {
        log.info("Загрузка документов для фонда: {}", charityId);
        log.info("Получены заголовки: {}", titles);
        log.info("Получены описания: {}", descriptions);
        
        // Проверяем существование фонда
        Charity charity = charityRepository.findById(charityId)
                .orElseThrow(() -> {
                    log.error("Фонд с ID {} не найден", charityId);
                    return new RuntimeException("Фонд не найден");
                });

        // Проверяем права доступа
        if (!charity.getCreatedBy().getId().equals(userId)) {
            log.error("Пользователь {} не имеет прав для загрузки документов в фонд {}", userId, charityId);
            throw new RuntimeException("У вас нет прав для загрузки документов в этот фонд");
        }

        // Загружаем документы
        List<Document> uploadedDocuments = new ArrayList<>();
        try {
            for (int i = 0; i < documents.size(); i++) {
                MultipartFile document = documents.get(i);
                String title = (titles != null && titles.size() > i) ? titles.get(i) : "";
                String description = (descriptions != null && descriptions.size() > i) ? descriptions.get(i) : "";

                log.info("Обработка документа {}: title='{}', description='{}'", i, title, description);

                // Генерируем уникальное имя файла
                String fileName = UUID.randomUUID().toString() + "_" + document.getOriginalFilename();
                Path uploadPath = Paths.get("uploads");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Сохраняем файл
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(document.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Создаем новый документ
                Document newDocument = new Document();
                newDocument.setUrl("/uploads/" + fileName);
                newDocument.setTitle(title);
                newDocument.setDescription(description);
                uploadedDocuments.add(newDocument);

                log.info("Документ успешно загружен: {}", newDocument);
            }

            // Обновляем список документов в основном фандрайзинге
            Fundraising generalFund = fundraisingRepository.findByCharityIdAndTargetAmount(
                charityId, new BigDecimal("999999999999"))
                .orElseThrow(() -> new RuntimeException("Основной фонд не найден"));

            generalFund.getDocuments().addAll(uploadedDocuments);
            fundraisingRepository.save(generalFund);

            // Обновляем список документов в самом фонде
            charity.getDocuments().addAll(uploadedDocuments);
            charityRepository.save(charity);

            log.info("Документы успешно добавлены в фонд {}", charityId);
            return convertToResponse(charity);

        } catch (IOException e) {
            log.error("Ошибка при загрузке документов: {}", e.getMessage());
            throw new RuntimeException("Не удалось загрузить документы: " + e.getMessage());
        }
    }

    private DocumentDTO convertDocumentToDTO(Document document) {
        if (document == null) return null;
        DocumentDTO dto = new DocumentDTO();
        dto.setUrl(document.getUrl());
        dto.setTitle(document.getTitle());
        dto.setDescription(document.getDescription());
        return dto;
    }

    private FundraisingResponse convertFundraisingToResponse(Fundraising fundraising) {
        FundraisingResponse fr = new FundraisingResponse();
        fr.setId(fundraising.getId());
        fr.setTitle(fundraising.getTitle());
        fr.setDescription(fundraising.getDescription());
        fr.setTargetAmount(fundraising.getTargetAmount());
        fr.setCurrentAmount(fundraising.getCurrentAmount());
        fr.setActive(fundraising.isActive());
        fr.setCompleted(fundraising.isCompleted());
        fr.setStartDate(fundraising.getStartDate());
        fr.setEndDate(fundraising.getEndDate());
        fr.setDiagnosis(fundraising.getDiagnosis());
        fr.setDocuments(fundraising.getDocuments().stream()
                .map(this::convertDocumentToDTO)
                .collect(Collectors.toList()));
        fr.setImageUrl(fundraising.getImageUrl());
        fr.setCreatedBy(fundraising.getCreatedBy());
        return fr;
    }

    private CharityResponse convertToResponse(Charity charity) {
        CharityResponse response = new CharityResponse();
        response.setId(charity.getId());
        response.setName(charity.getName());
        response.setDescription(charity.getDescription());
        response.setWebsiteUrl(charity.getWebsiteUrl());
        response.setCategories(charity.getCategories());
        response.setRegistrationNumber(charity.getRegistrationNumber());
        response.setContactEmail(charity.getContactEmail());
        response.setContactPhone(charity.getContactPhone());
        response.setContactAddress(charity.getContactAddress());
        response.setOrganizationName(charity.getOrganizationName());
        response.setInn(charity.getInn());
        response.setKpp(charity.getKpp());
        response.setAccountNumber(charity.getAccountNumber());
        response.setBik(charity.getBik());
        response.setBankName(charity.getBankName());
        response.setCreatedBy(charity.getCreatedBy());
        response.setVerified(charity.isVerified());
        response.setActive(charity.isActive());
        response.setDocuments(charity.getDocuments().stream()
                .map(this::convertDocumentToDTO)
                .collect(Collectors.toList()));

        // Получаем все фандрайзинги для этого фонда
        List<Fundraising> fundraisings = fundraisingRepository.findByCharityId(charity.getId());
        List<FundraisingResponse> fundraisingResponses = fundraisings.stream()
                .map(this::convertFundraisingToResponse)
                .collect(Collectors.toList());
        response.setFundraisings(fundraisingResponses);

        // Подсчитываем статистику
        BigDecimal totalDonations = BigDecimal.ZERO;
        Set<Long> uniqueDonors = new HashSet<>();
        int recurringDonationsCount = 0;
        int completedFundraisingsCount = 0;

        for (Fundraising fundraising : fundraisings) {
            // Считаем общую сумму пожертвований
            BigDecimal fundraisingTotal = donationRepository.getTotalAmountByFundraisingId(fundraising.getId());
            if (fundraisingTotal != null) {
                totalDonations = totalDonations.add(fundraisingTotal);
            }

            // Считаем уникальных доноров и регулярные пожертвования
            List<Donation> donations = donationRepository.findByFundraisingId(fundraising.getId());
            for (Donation donation : donations) {
                uniqueDonors.add(donation.getUser().getId());
                if (donation.isRecurring()) {
                    recurringDonationsCount++;
                }
            }

            // Считаем завершенные сборы
            if (fundraising.isCompleted()) {
                completedFundraisingsCount++;
            }
        }

        response.setTotalDonations(totalDonations);
        response.setTotalDonors((long) uniqueDonors.size());
        response.setRecurringDonationsCount(recurringDonationsCount);
        response.setCompletedFundraisingsCount(completedFundraisingsCount);

        return response;
    }

    private double calculateTotalDonations(Long charityId) {
        List<Long> fundraisingIds = fundraisingRepository.findByCharityId(charityId)
                .stream()
                .map(f -> f.getId())
                .collect(Collectors.toList());
        
        return fundraisingIds.stream()
                .map(id -> donationRepository.getTotalAmountByFundraisingId(id))
                .filter(amount -> amount != null)
                .mapToDouble(amount -> amount.doubleValue())
                .sum();
    }
} 