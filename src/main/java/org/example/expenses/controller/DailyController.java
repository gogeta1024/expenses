package org.example.expenses.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.expenses.dto.DailyDTO;
import org.example.expenses.entity.DailyCost;
import org.example.expenses.service.DailyService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("daily")
@RequiredArgsConstructor
public class DailyController {
    private final DailyService dailyService;

    @ModelAttribute("daily")
    public DailyDTO setupDailyDTO() {
        return new DailyDTO(); // Tạo đối tượng mặc định nếu chưa có trong session
    }

    public void data(Model model) {

    }

    @GetMapping
    public String daily( @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "10") int size,
                         Model model) {
        // Lấy giá trị ngày hôm nay gán cho purchaseDate
        LocalDate purchaseDate = LocalDate.now();

        // Tính toán month và year từ purchaseDate
        int month = purchaseDate.getMonthValue();
        int year = purchaseDate.getYear();

        List<DailyCost> dailyCostList = dailyService.findByMonthAndYear(month, year);

        Pageable pageable = PageRequest.of(page,size, Sort.by("purchaseDate").descending());
        Page<DailyCost> dailyPage = dailyService.getPaginatedDailyList(dailyCostList, pageable); // 10 items per page
        model.addAttribute("dailyList", dailyPage.getContent());
        model.addAttribute("totalPages", dailyPage.getTotalPages());
        model.addAttribute("currentPage", dailyPage.getNumber());

        Integer totalAmount = dailyService.totalAmount(dailyCostList);
        model.addAttribute("total", totalAmount);
        return "daily";
    }

    @PostMapping("/save")
    public String save(@Validated  @ModelAttribute("daily") DailyDTO dailyDTO,
                       BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors())
        {
            model.addAttribute("org.springframework.validation.BindingResult.daily", bindingResult);
            return "daily";
        }

        DailyCost dailyCost = DailyService.convertToEntity(dailyDTO);
        dailyService.save(dailyCost);

        return "redirect:/daily";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Model model)
    {
        dailyService.deleteByID(id);
        return "redirect:/daily";
    }

    @GetMapping(value = "edit/{id}")
    public String dailyEdit(@PathVariable Long id, Model model) {
        DailyCost dailyEdit = dailyService.findByID(id);
        System.out.println("purchaseDate: " + dailyEdit.getPurchaseDate());
        model.addAttribute("dailyEdit",dailyEdit);
        return "daily-edit";
    }

    @PostMapping("/edit")
    public String dailyUpdate(
            @Validated @ModelAttribute("dailyEdit") DailyDTO dailyDTO,
            BindingResult bindingResult, Model model) {


        if(bindingResult.hasErrors())
        {
            model.addAttribute("org.springframework.validation.BindingResult.dailyEdit", bindingResult); //bên hTML phải thêm trường ẩn mới get dc id
            return "daily-edit";
        }

        DailyCost dailyEdit = dailyDTO.getId()!=null ? dailyService.findByID(dailyDTO.getId()) : new DailyCost();

        BeanUtils.copyProperties(dailyDTO, dailyEdit,"id");
        dailyService.save(dailyEdit);

        return "redirect:/daily";
    }


}




