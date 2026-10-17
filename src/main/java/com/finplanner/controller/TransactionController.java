package com.finplanner.controller;

import com.finplanner.model.Transaction;
import com.finplanner.model.TransactionType;
import com.finplanner.model.User;
import com.finplanner.service.FinanceService;
import com.finplanner.service.UserService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    private final FinanceService financeService;
    private final UserService userService;

    public TransactionController(FinanceService financeService, UserService userService) {
        this.financeService = financeService;
        this.userService = userService;
    }

    @GetMapping
    public String transactions(Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String search) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        List<Transaction> transactions = financeService.getTransactions(user);

        // Apply optional filters via stream
        if (type != null && !type.isBlank()) {
            try {
                TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
                transactions = transactions.stream()
                        .filter(t -> t.getType() == transactionType)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {
                // Unknown type value — skip filter
            }
        }

        if (category != null && !category.isBlank()) {
            transactions = transactions.stream()
                    .filter(t -> category.equalsIgnoreCase(t.getCategory()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String lowerSearch = search.toLowerCase();
            transactions = transactions.stream()
                    .filter(t -> t.getDescription() != null
                            && t.getDescription().toLowerCase().contains(lowerSearch))
                    .collect(Collectors.toList());
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("categories", financeService.getCategories(user));
        model.addAttribute("transaction", new Transaction());

        // Preserve filter state for form pre-population
        model.addAttribute("filterType", type);
        model.addAttribute("filterCategory", category);
        model.addAttribute("filterSearch", search);

        return "transactions";
    }

    @GetMapping("/{id}/edit")
    public String editTransaction(@PathVariable Long id, Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        Transaction transaction = financeService.getTransactions(user).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        model.addAttribute("transactions", financeService.getTransactions(user));
        model.addAttribute("categories", financeService.getCategories(user));
        model.addAttribute("transaction", transaction);
        return "transactions";
    }

    @PostMapping("/save")
    public String saveTransaction(@ModelAttribute("transaction") Transaction transaction,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(userDetails.getUsername());

        try {
            financeService.saveTransaction(transaction, user);
            redirectAttributes.addFlashAttribute("success", "Transaction saved successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/transactions";
    }

    @PostMapping("/{id}/delete")
    public String deleteTransaction(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        financeService.deleteTransaction(id, user);
        redirectAttributes.addFlashAttribute("success", "Transaction deleted.");
        return "redirect:/transactions";
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(@AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User user = userService.getCurrentUser(userDetails.getUsername());
        List<Transaction> transactions = financeService.getTransactions(user);

        StringWriter stringWriter = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("Date", "Type", "Description", "Category", "Amount");
            for (Transaction transaction : transactions) {
                csvPrinter.printRecord(
                        transaction.getDate() != null ? transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                        transaction.getType() != null ? transaction.getType().name() : "",
                        transaction.getDescription() != null ? transaction.getDescription() : "",
                        transaction.getCategory() != null ? transaction.getCategory() : "",
                        transaction.getAmount() != null ? transaction.getAmount().toPlainString() : "0.00"
                );
            }
        }

        byte[] csvBody = stringWriter.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBody);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@AuthenticationPrincipal UserDetails userDetails) throws IOException, DocumentException {
        User user = userService.getCurrentUser(userDetails.getUsername());
        List<Transaction> transactions = financeService.getTransactions(user);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Financial Planner - Transactions"));

        PdfPTable table = new PdfPTable(5);
        table.addCell("Date");
        table.addCell("Type");
        table.addCell("Description");
        table.addCell("Category");
        table.addCell("Amount");

        for (Transaction transaction : transactions) {
            table.addCell(transaction.getDate() != null ? transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
            table.addCell(transaction.getType() != null ? transaction.getType().name() : "");
            table.addCell(transaction.getDescription() != null ? transaction.getDescription() : "");
            table.addCell(transaction.getCategory() != null ? transaction.getCategory() : "");
            table.addCell(transaction.getAmount() != null ? transaction.getAmount().toPlainString() : "0.00");
        }

        document.add(table);
        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(outputStream.toByteArray());
    }
}
