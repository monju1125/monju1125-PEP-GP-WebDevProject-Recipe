-- Create the Chef Table:
-- This table stores information about chefs.
-- 
-- Fields:
--      1. id: An auto-incremented primary key to uniquely identify each chef.
--      2. username: A unique and non-nullable varchar field to store the chef's username.
--      3. email: A unique and non-nullable varchar field to store the chef's email address.
--      4. password: A non-nullable varchar field to store the chef's password.
--      5. is_admin: A boolean field to indicate if the chef has admin privileges.

CREATE TABLE CHEF (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_admin BOOLEAN
);


-- Create the Recipe Table:
-- This table stores information about recipes.
-- 
-- Fields:
--      1. id: An auto-incremented primary key to uniquely identify each recipe.
--      2. name: A unique and non-nullable varchar field to store the recipe's name.
--      3. instructions: A non-nullable varchar field to store the recipe's instructions.
--      4. chef_id: A foreign key that references the 'id' field from the Chef table. Ensure that referential integrity is maintained by cascading deletions.
CREATE TABLE RECIPE (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    instructions VARCHAR(255) NOT NULL,
    chef_id INT,
    FOREIGN KEY (chef_id) REFERENCES CHEF(id)
);

-- Create Ingredient Table:
--  This table stores information about ingredients.
-- Fields:
--      1. id: An auto-incremented primary key to uniquely identify each ingredient.
--      2. name: A unique and non-nullable varchar field (max 20 characters) to store the ingredient's name.
CREATE TABLE INGREDIENT (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Recipe_Ingredient Table
-- This table represents the many-to-many relationship between recipes and ingredients.
-- It stores the relationship between a recipe and its ingredients along with the volume and unit.
-- Fields:
-- 1. id: An auto-incremented primary key for each entry in the join table.
-- 2. recipe_id: A non-nullable foreign key that references the 'id' field from the Recipe table.
-- 3. ingredient_id: A non-nullable foreign key that references the 'id' field from the Ingredient table.
-- 4. vol: A decimal field to store the volume of the ingredient used in the recipe.
-- 5. unit: A non-nullable varchar field (max 20 characters) to store the unit of the volume.
-- 6. is_metric: A boolean field to indicate if the unit is in metric. Defaults to false.
CREATE TABLE RECIPE_INGREDIENT (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    vol DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    is_metric BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (recipe_id) REFERENCES RECIPE(id),
    FOREIGN KEY (ingredient_id) REFERENCES INGREDIENT(id)
);

-- DO NOT EDIT ANY CODE BELOW THIS LINE!
-- The below code inserts values into the tables you define.

-- Delete all records from all tables.
DELETE FROM RECIPE_INGREDIENT;
DELETE FROM RECIPE;
DELETE FROM CHEF;
DELETE FROM INGREDIENT;

-- Resets the starting id value for each table.
ALTER TABLE CHEF ALTER COLUMN id RESTART WITH 1;
ALTER TABLE INGREDIENT ALTER COLUMN id RESTART WITH 1;
ALTER TABLE RECIPE ALTER COLUMN id RESTART WITH 1;
ALTER TABLE RECIPE_INGREDIENT ALTER COLUMN id RESTART WITH 1;

-- populate tables
INSERT INTO 
CHEF 
	(username, email, password, is_admin) 
VALUES 
	('JoeCool', 'snoopy@null.com', 'redbarron',false),
	('CharlieBrown', 'goodgrief@peanuts.com', 'thegreatpumpkin', false),
	('RevaBuddy', 'revature@revature.com', 'codelikeaboss', false),
	('ChefTrevin', 'trevin@revature.com', 'trevature', true);

INSERT INTO 
RECIPE
	(name, instructions, chef_id) 
VALUES 
	('carrot soup', 'Put carrot in water.  Boil.  Maybe salt.',1),
	('potato soup', 'Put potato in water.  Boil.  Maybe salt.', 2),
	('tomato soup', 'Put tomato in water.  Boil.  Maybe salt.', 2),
	('lemon rice soup', 'Put lemon and rice in water.  Boil.  Maybe salt.', 4),
	('stone soup', 'Put stone in water.  Boil.  Maybe salt.', 4);

INSERT INTO
INGREDIENT
	(name)
VALUES
	('carrot'),
	('potato'),
	('tomato'),
	('lemon'),
	('rice'),
	('stone');


INSERT INTO
RECIPE_INGREDIENT
    (id, recipe_id, ingredient_id, vol, unit)
VALUES
    (default, 1, 1, 1, 'cups'),
    (default, 2, 2, 2, 'cups'),
    (default, 3, 3, 2, 'cups'),
    (default, 4, 4, 1, 'Tbs'),
    (default, 4, 5, 2, 'cups');

