package baguchan.structure_lib.util;

import com.mojang.nbt.*;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class RevampStructure {
	public String modId;
	public String translateKey;
	public String filePath;
	public CompoundTag data;
	public boolean placeAir;
	public boolean replaceBlocks;

	public RevampStructure(String modId, String translateKey, CompoundTag data, boolean placeAir, boolean replaceBlocks) {
		this.modId = modId;
		this.translateKey = "structure." + modId + "." + translateKey + ".name";
		this.data = data;
		this.filePath = null;
		this.placeAir = placeAir;
		this.replaceBlocks = replaceBlocks;
	}

	public RevampStructure(String modId, String translateKey, String filePath, boolean placeAir, boolean replaceBlocks) {
		this.modId = modId;
		this.translateKey = "structure." + modId + "." + translateKey + ".name";
		this.placeAir = placeAir;
		this.replaceBlocks = replaceBlocks;
		this.loadFromNBT(filePath);
	}

	public boolean placeStructure(World world, int originX, int originY, int originZ) {
		Vec3i origin = new Vec3i(originX, originY, originZ);
		ArrayList<BlockInstance> blocks = this.getBlocks(origin);
		Iterator var7 = blocks.iterator();

		BlockInstance block;
		while (var7.hasNext()) {
			block = (BlockInstance) var7.next();
			if (!this.replaceBlocks && world.getBlockId(block.pos.x, block.pos.y, block.pos.z) != 0) {
				return false;
			}
		}

		var7 = blocks.iterator();

		while (var7.hasNext()) {
			block = (BlockInstance) var7.next();
			world.setBlockAndMetadataWithNotify(block.pos.x, block.pos.y, block.pos.z, block.block.id, block.meta);
		}

		return true;
	}

	public boolean placeStructure(World world, int originX, int originY, int originZ, String direction) {
		Direction dir = Direction.getFromName(direction);
		if (dir == null) {
			return false;
		} else {
			Vec3i origin = new Vec3i(originX, originY, originZ);
			ArrayList<BlockInstance> blocks = this.getBlocks(origin, dir);
			Iterator var9 = blocks.iterator();

			BlockInstance block;
			do {
				if (!var9.hasNext()) {
					var9 = blocks.iterator();

					while (var9.hasNext()) {
						block = (BlockInstance) var9.next();
						world.setBlockAndMetadataWithNotify(block.pos.x, block.pos.y, block.pos.z, block.block.id, block.meta == -1 ? 0 : block.meta);
					}

					return true;
				}

				block = (BlockInstance) var9.next();
			} while (this.replaceBlocks || world.getBlockId(block.pos.x, block.pos.y, block.pos.z) == 0);

			return false;
		}
	}

	public BlockInstance getOrigin() {
		CompoundTag blockTag = this.data.getCompound("Origin");
		int meta = blockTag.getInteger("meta");
		int id = getBlockId(blockTag);
		Block block = Block.getBlock(id);
		return new BlockInstance(block, new Vec3i(), meta, (TileEntity) null);
	}

	public BlockInstance getOrigin(Vec3i origin) {
		CompoundTag blockTag = this.data.getCompound("Origin");
		int meta = blockTag.getInteger("meta");
		int id = getBlockId(blockTag);
		Block block = Block.getBlock(id);
		return new BlockInstance(block, origin, meta, (TileEntity) null);
	}

	public BlockInstance getOrigin(World world, Vec3i origin) {
		CompoundTag blockTag = this.data.getCompound("Origin");
		Vec3i pos = new Vec3i(blockTag.getCompound("pos"));
		int meta = blockTag.getInteger("meta");
		int id = getBlockId(blockTag);
		Block block = Block.getBlock(id);
		return new BlockInstance(block, pos, meta, world.getBlockTileEntity(pos.x, pos.y, pos.z));
	}

	public ArrayList<BlockInstance> getTileEntities() {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var2 = this.data.getList("TileEntities").iterator();

		while (var2.hasNext()) {
			Tag<?> tag = (Tag) var2.next();
			CompoundTag tileEntity = (CompoundTag) tag;
			Vec3i pos = new Vec3i(tileEntity.getCompound("pos"));
			int meta = tileEntity.getInteger("meta");
			int id = getBlockId(tileEntity);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, (TileEntity) null);
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public ArrayList<BlockInstance> getTileEntities(Vec3i origin) {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var3 = this.data.getList("TileEntities").iterator();

		while (var3.hasNext()) {
			Tag<?> tag = (Tag) var3.next();
			CompoundTag tileEntity = (CompoundTag) tag;
			Vec3i pos = (new Vec3i(tileEntity.getCompound("pos"))).add(origin);
			int meta = tileEntity.getInteger("meta");
			int id = getBlockId(tileEntity);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, (TileEntity) null);
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public ArrayList<BlockInstance> getTileEntities(World world, Vec3i origin) {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var4 = this.data.getList("TileEntities").iterator();

		while (var4.hasNext()) {
			Tag<?> tag = (Tag) var4.next();
			CompoundTag tileEntity = (CompoundTag) tag;
			Vec3i pos = (new Vec3i(tileEntity.getCompound("pos"))).add(origin);
			int meta = tileEntity.getInteger("meta");
			int id = getBlockId(tileEntity);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, world.getBlockTileEntity(pos.x, pos.y, pos.z));
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public ArrayList<BlockInstance> getTileEntities(World world, Vec3i origin, Direction dir) {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var5 = this.data.getList("Blocks").iterator();

		while (var5.hasNext()) {
			Tag<?> tag = (Tag) var5.next();
			CompoundTag blockTag = (CompoundTag) tag;
			Vec3i pos = (new Vec3i(blockTag.getCompound("pos"))).rotate(origin, dir);
			int meta = blockTag.getInteger("meta");
			if (meta != -1) {
				if (dir == Direction.Z_NEG) {
					meta = Direction.getDirectionFromSide(meta).getOpposite().getSideNumber();
				} else if (dir == Direction.X_NEG || dir == Direction.X_POS) {
					Direction blockDir = Direction.getDirectionFromSide(meta);
					blockDir = blockDir != Direction.X_NEG && blockDir != Direction.X_POS ? blockDir.rotate(1) : blockDir.rotate(1).getOpposite();
					meta = dir == Direction.X_NEG ? blockDir.getSideNumber() : blockDir.getOpposite().getSideNumber();
				}
			}

			int id = getBlockId(blockTag);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, world.getBlockTileEntity(pos.x, pos.y, pos.z));
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public ArrayList<BlockInstance> getBlocks() {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var2 = this.data.getList("Blocks").iterator();

		while (var2.hasNext()) {
			Tag<?> tag = (Tag) var2.next();
			CompoundTag blockTag = (CompoundTag) tag;
			Vec3i pos = new Vec3i(blockTag.getCompound("pos"));
			int meta = blockTag.getInteger("meta");
			int id = getBlockId(blockTag);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, (TileEntity) null);
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public ArrayList<BlockInstance> getBlocks(Vec3i origin) {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var3 = this.data.getList("Blocks").iterator();

		while (var3.hasNext()) {
			Tag<?> tag = (Tag) var3.next();
			CompoundTag blockTag = (CompoundTag) tag;
			Vec3i pos = (new Vec3i(blockTag.getCompound("pos"))).add(origin);
			int meta = blockTag.getInteger("meta");
			int id = getBlockId(blockTag);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, (TileEntity) null);
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public ArrayList<BlockInstance> getBlocks(Vec3i origin, Direction dir) {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var4 = this.data.getList("Blocks").iterator();

		while (var4.hasNext()) {
			Tag<?> tag = (Tag) var4.next();
			CompoundTag blockTag = (CompoundTag) tag;
			Vec3i pos = (new Vec3i(blockTag.getCompound("pos"))).rotate(origin, dir);
			int meta = blockTag.getInteger("meta");
			if (meta != -1) {
				if (dir == Direction.Z_NEG) {
					meta = Direction.getDirectionFromSide(meta).getOpposite().getSideNumber();
				} else if (dir == Direction.X_NEG || dir == Direction.X_POS) {
					Direction blockDir = Direction.getDirectionFromSide(meta);
					blockDir = blockDir != Direction.X_NEG && blockDir != Direction.X_POS ? blockDir.rotate(1) : blockDir.rotate(1).getOpposite();
					meta = dir == Direction.X_NEG ? blockDir.getSideNumber() : blockDir.getOpposite().getSideNumber();
				}
			}

			int id = getBlockId(blockTag);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, (TileEntity) null);
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public ArrayList<BlockInstance> getSubstitutions() {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var2 = this.data.getCompound("Substitutions").getValues().iterator();

		while (var2.hasNext()) {
			Tag<?> tag = (Tag) var2.next();
			CompoundTag sub = (CompoundTag) tag;
			Vec3i pos = new Vec3i(sub.getCompound("pos"));
			int meta = sub.getInteger("meta");
			int id = getBlockId(sub);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, (TileEntity) null);
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public ArrayList<BlockInstance> getSubstitutions(Vec3i origin) {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var3 = this.data.getCompound("Substitutions").getValues().iterator();

		while (var3.hasNext()) {
			Tag<?> tag = (Tag) var3.next();
			CompoundTag sub = (CompoundTag) tag;
			Vec3i pos = (new Vec3i(sub.getCompound("pos"))).add(origin);
			int meta = sub.getInteger("meta");
			int id = getBlockId(sub);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, (TileEntity) null);
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public ArrayList<BlockInstance> getSubstitutions(Vec3i origin, Direction dir) {
		ArrayList<BlockInstance> tiles = new ArrayList();
		Iterator var4 = this.data.getCompound("Substitutions").getValues().iterator();

		while (var4.hasNext()) {
			Tag<?> tag = (Tag) var4.next();
			CompoundTag tileEntity = (CompoundTag) tag;
			Vec3i pos = (new Vec3i(tileEntity.getCompound("pos"))).rotate(origin, dir);
			int meta = tileEntity.getInteger("meta");
			if (meta != -1) {
				if (dir == Direction.Z_NEG) {
					meta = Direction.getDirectionFromSide(meta).getOpposite().getSideNumber();
				} else if (dir == Direction.X_NEG || dir == Direction.X_POS) {
					Direction blockDir = Direction.getDirectionFromSide(meta);
					blockDir = blockDir != Direction.X_NEG && blockDir != Direction.X_POS ? blockDir.rotate(1) : blockDir.rotate(1).getOpposite();
					meta = dir == Direction.X_NEG ? blockDir.getSideNumber() : blockDir.getOpposite().getSideNumber();
				}
			}

			int id = getBlockId(tileEntity);
			Block block = Block.getBlock(id);
			BlockInstance blockInstance = new BlockInstance(block, pos, meta, (TileEntity) null);
			tiles.add(blockInstance);
		}

		return tiles;
	}

	public static int getBlockId(CompoundTag block) {
		Tag<?> nbt = block.getTag("id");
		if (nbt instanceof IntTag) {
			return (Integer) ((IntTag) nbt).getValue();
		} else if (nbt instanceof StringTag) {
			String args = ((String) ((StringTag) nbt).getValue());
			Block b = Block.getBlockByName(args);
			return b.id;
		} else {
			return 0;
		}
	}

	protected void loadFromNBT(String name) {
		try {
			InputStream resource = this.getClass().getResourceAsStream("/assets/" + this.modId + "/structures/" + name + ".nbt");

			try {
				if (resource != null) {
					this.data = NbtIo.readCompressed(resource);
				}
			} catch (Throwable var6) {
				if (resource != null) {
					try {
						resource.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}
				}

				throw var6;
			}

			if (resource != null) {
				resource.close();
			}
		} catch (IOException var7) {
			IOException e = var7;
			e.printStackTrace();
		}

	}
}