package com.revature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.revature.model.Chef;
import com.revature.dao.ChefDAO;
import com.revature.service.ChefService;
import com.revature.util.Page;
import com.revature.util.PageOptions;

public class ChefServiceTest {

    private ChefService chefService;
    private ChefDAO chefDao;
    List<Chef> MOCKS;

    @BeforeEach
    void setUpMocks() {
        chefDao = mock(ChefDAO.class);
        chefService = new ChefService(chefDao);
        MOCKS = Arrays.asList(
                new Chef(1, "JoeCool", "snoopy@null.com", "redbarron", false),
                new Chef(2, "CharlieBrown", "goodgrief@peanuts.com", "thegreatpumpkin", false),
                new Chef(3, "RevaBuddy", "revature@revature.com", "codelikeaboss", false),
                new Chef(4, "ChefTrevin", "trevin@revature.com", "trevature", true));
    }

    @Test
    public void fetchOneChef() {
        when(chefDao.getChefById(1)).thenReturn(MOCKS.get(0));
        Optional<Chef> chef = chefService.findChef(1);
        assertTrue(chef.isPresent(), () -> "Chef should be present");
        assertEquals(MOCKS.get(0), chef.get(), () -> "Chef should match");
    }

    @Test
    public void failToFetchOneChef() {
        when(chefDao.getChefById(1)).thenReturn(null);
        Optional<Chef> chef = chefService.findChef(1);
        assertTrue(chef.isEmpty(), () -> "Chef should not be present");
    }

    @Test
    public void saveNewChef() {
        Chef newChef = new Chef(0, "new chef", "newchef@chefscape.net", "1234abc", false);
        ArgumentCaptor<Chef> chefCaptor = ArgumentCaptor.forClass(Chef.class);
        when(chefDao.createChef(any(Chef.class))).thenReturn(42);
        chefService.saveChef(newChef);
        verify(chefDao).createChef(chefCaptor.capture());
        Chef captureChef = chefCaptor.getValue();
        assertEquals(42, captureChef.getId(), () -> "Services should set the id of newly created chef");
    }

    @Test
    public void updateChef() {
        Chef existingChef = new Chef(42, "Existing Chef", "Existing.Chef@gmail.com", "1234abc", false);
        ArgumentCaptor<Chef> chefCaptor = ArgumentCaptor.forClass(Chef.class);
        chefService.saveChef(existingChef);
        verify(chefDao).updateChef(chefCaptor.capture());
        Chef captureChef = chefCaptor.getValue();
        assertEquals(42, captureChef.getId(), () -> "Services should not change the id of existing chef");
    }

    @Test
    public void deleteChef() {
        when(chefDao.getChefById(1)).thenReturn(MOCKS.get(0));
        doNothing().when(chefDao).deleteChef(any(Chef.class));
        ArgumentCaptor<Chef> chefCaptor = ArgumentCaptor.forClass(Chef.class);
        chefService.deleteChef(1);
        verify(chefDao).deleteChef(chefCaptor.capture());
        verify(chefDao).getChefById(1);
    }

    @Test
    public void searchForListOfAllChefs() {
        when(chefDao.getAllChefs()).thenReturn(MOCKS);
        List<Chef> chefs = chefService.searchChefs(null);
        assertIterableEquals(MOCKS, chefs, () -> "Chefs should match");
    }

    @Test
    public void searchForFilteredListOfChefs() {
        when(chefDao.searchChefsByTerm("a")).thenReturn(Arrays.asList(MOCKS.get(1), MOCKS.get(2)));
        List<Chef> chefs = chefService.searchChefs("a");
        assertIterableEquals(Arrays.asList(MOCKS.get(1), MOCKS.get(2)), chefs, () -> "Chefs should match");
    }

    @Test
    public void searchReturnsEmptyList() {
        when(chefDao.searchChefsByTerm("Bal")).thenReturn(Collections.emptyList());
        List<Chef> chefs = chefService.searchChefs("Bal");
        assertTrue(chefs.isEmpty(), () -> "Chefs should be empty");
    }

    @Test
    public void searchForPageOfAllChefs() {
        when(chefDao.getAllChefs(any(PageOptions.class))).thenReturn(new Page<Chef>(1, 4, 1, 4, MOCKS));
        Page<Chef> chefs = chefService.searchChefs(null, 1, 4, "id", "asc");
        ArgumentCaptor<PageOptions> optionsCaptor = ArgumentCaptor.forClass(PageOptions.class);
        verify(chefDao).getAllChefs(optionsCaptor.capture());
        assertEquals(new Page<Chef>(1, 4, 1, 4, MOCKS), chefs,
                () -> "Service shouldn't change the page returned from the dao");
    }

    @Test
    public void searchForFilteredPageOfChef() {
        when(chefDao.searchChefsByTerm(anyString(), any(PageOptions.class)))
                .thenReturn(new Page<Chef>(1, 2, 1, 2, Arrays.asList(MOCKS.get(1), MOCKS.get(2))));
        Page<Chef> chefs = chefService.searchChefs("a", 1, 2, "id", "asc");
        ArgumentCaptor<PageOptions> optionsCaptor = ArgumentCaptor.forClass(PageOptions.class);
        ArgumentCaptor<String> termCaptor = ArgumentCaptor.forClass(String.class);
        verify(chefDao).searchChefsByTerm(termCaptor.capture(), optionsCaptor.capture());
        assertEquals(new Page<Chef>(1, 2, 1, 2, Arrays.asList(MOCKS.get(1), MOCKS.get(2))), chefs,
                () -> "Service shouldn't change the page returned from the dao");
    }

    @Test
    public void searchReturnsEmptyPage() {
        when(chefDao.searchChefsByTerm(anyString(), any(PageOptions.class)))
                .thenReturn(new Page<Chef>(1, 5, 0, 0, Collections.emptyList()));
        Page<Chef> chefs = chefService.searchChefs("Bal", 1, 5, "id", "asc");
        ArgumentCaptor<PageOptions> optionsCaptor = ArgumentCaptor.forClass(PageOptions.class);
        ArgumentCaptor<String> termCaptor = ArgumentCaptor.forClass(String.class);
        verify(chefDao).searchChefsByTerm(termCaptor.capture(), optionsCaptor.capture());
        assertEquals(new Page<Chef>(1, 5, 0, 0, Collections.emptyList()), chefs,
                () -> "Service shouldn't change the page returned from the dao");
    }

}
